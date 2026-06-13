package com.sabormayor.menu.application;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ConflictException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.menu.config.CacheConfig;
import com.sabormayor.menu.domain.Category;
import com.sabormayor.menu.domain.Dish;
import com.sabormayor.menu.domain.DishPriceHistory;
import com.sabormayor.menu.infrastructure.CategoryRepository;
import com.sabormayor.menu.infrastructure.DishPriceHistoryRepository;
import com.sabormayor.menu.infrastructure.DishRepository;
import com.sabormayor.menu.mapper.MenuMapper;
import com.sabormayor.menu.web.dto.CategoryRequest;
import com.sabormayor.menu.web.dto.CategoryResponse;
import com.sabormayor.menu.web.dto.DishMarginResponse;
import com.sabormayor.menu.web.dto.DishRequest;
import com.sabormayor.menu.web.dto.DishResponse;

import lombok.RequiredArgsConstructor;

/**
 * Public reads are cached in Redis ("menu" cache); every write evicts the
 * whole cache so the public menu is always consistent after an edit.
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final DishPriceHistoryRepository priceHistoryRepository;
    private final MenuMapper mapper;

    // ---------- Public reads (cached) ----------

    @Cacheable(cacheNames = CacheConfig.MENU_CACHE, key = "'categories'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return mapper.toCategoryResponses(categoryRepository.findAllByOrderByDisplayOrderAsc());
    }

    @Cacheable(cacheNames = CacheConfig.MENU_CACHE,
            key = "'dishes:' + (#categorySlug ?: '*') + ':' + (#tag ?: '*') + ':' + #onlyAvailable")
    @Transactional(readOnly = true)
    public List<DishResponse> getDishes(String categorySlug, String tag, boolean onlyAvailable) {
        return mapper.toDishResponses(dishRepository.search(categorySlug, tag, onlyAvailable));
    }

    @Cacheable(cacheNames = CacheConfig.MENU_CACHE, key = "'dish:' + #slug")
    @Transactional(readOnly = true)
    public DishResponse getDishBySlug(String slug) {
        return mapper.toResponse(dishRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Dish", slug)));
    }

    /** Uncached batch lookup used by order-service for price validation. */
    @Transactional(readOnly = true)
    public List<DishResponse> getDishesByIds(List<UUID> ids) {
        return mapper.toDishResponses(dishRepository.findAllById(ids));
    }

    // ---------- Admin writes (evict cache) ----------

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public DishResponse createDish(DishRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()));
        String slug = slugify(request.name());
        if (dishRepository.existsBySlug(slug)) {
            throw new ConflictException("A dish with slug '%s' already exists".formatted(slug));
        }
        Dish dish = Dish.builder()
                .category(category)
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .price(request.price())
                .cost(request.cost())
                .available(request.available())
                .featured(request.featured())
                .imageUrl(request.imageUrl())
                .prepMinutes(request.prepMinutes())
                .build();
        applyCollections(dish, request);
        Dish saved = dishRepository.save(dish);
        priceHistoryRepository.save(DishPriceHistory.builder().dishId(saved.getId()).price(saved.getPrice()).build());
        return mapper.toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public DishResponse updateDish(UUID dishId, DishRequest request) {
        Dish dish = requireDish(dishId);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()));
        boolean priceChanged = dish.getPrice().compareTo(request.price()) != 0;
        dish.setCategory(category);
        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setPrice(request.price());
        dish.setCost(request.cost());
        dish.setAvailable(request.available());
        dish.setFeatured(request.featured());
        dish.setImageUrl(request.imageUrl());
        dish.setPrepMinutes(request.prepMinutes());
        applyCollections(dish, request);
        if (priceChanged) {
            priceHistoryRepository.save(
                    DishPriceHistory.builder().dishId(dish.getId()).price(request.price()).build());
        }
        return mapper.toResponse(dish);
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public DishResponse updateAvailability(UUID dishId, boolean available) {
        Dish dish = requireDish(dishId);
        dish.setAvailable(available);
        return mapper.toResponse(dish);
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public DishResponse updatePrice(UUID dishId, java.math.BigDecimal newPrice, UUID changedBy) {
        Dish dish = requireDish(dishId);
        dish.setPrice(newPrice);
        priceHistoryRepository.save(
                DishPriceHistory.builder().dishId(dishId).price(newPrice).changedBy(changedBy).build());
        return mapper.toResponse(dish);
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public void deleteDish(UUID dishId) {
        dishRepository.delete(requireDish(dishId));
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = slugify(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("A category with slug '%s' already exists".formatted(slug));
        }
        return mapper.toResponse(categoryRepository.save(Category.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .displayOrder(request.displayOrder())
                .build()));
    }

    @CacheEvict(cacheNames = CacheConfig.MENU_CACHE, allEntries = true)
    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
        category.setName(request.name());
        category.setDescription(request.description());
        category.setDisplayOrder(request.displayOrder());
        return mapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<DishPriceHistory> getPriceHistory(UUID dishId) {
        requireDish(dishId);
        return priceHistoryRepository.findByDishIdOrderByChangedAtDesc(dishId);
    }

    @Transactional(readOnly = true)
    public List<DishMarginResponse> getMargins() {
        return mapper.toMarginResponses(dishRepository.findAll());
    }

    private Dish requireDish(UUID dishId) {
        return dishRepository.findById(dishId)
                .orElseThrow(() -> ResourceNotFoundException.of("Dish", dishId));
    }

    private void applyCollections(Dish dish, DishRequest request) {
        dish.getTags().clear();
        if (request.tags() != null) {
            dish.getTags().addAll(request.tags());
        }
        dish.getAllergens().clear();
        if (request.allergens() != null) {
            dish.getAllergens().addAll(request.allergens());
        }
        dish.getPairingIds().clear();
        if (request.pairingIds() != null) {
            dish.getPairingIds().addAll(request.pairingIds());
        }
    }

    static String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized;
    }
}
