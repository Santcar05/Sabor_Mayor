package com.sabormayor.inventory.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.BusinessRuleException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.common.events.OrderLine;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.inventory.domain.DishRecipe;
import com.sabormayor.inventory.domain.Ingredient;
import com.sabormayor.inventory.domain.StockMovement;
import com.sabormayor.inventory.infrastructure.DishRecipeRepository;
import com.sabormayor.inventory.infrastructure.IngredientRepository;
import com.sabormayor.inventory.infrastructure.StockMovementRepository;
import com.sabormayor.inventory.web.dto.IngredientRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final IngredientRepository ingredientRepository;
    private final DishRecipeRepository recipeRepository;
    private final StockMovementRepository movementRepository;

    @Transactional(readOnly = true)
    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Ingredient> lowStock() {
        return ingredientRepository.findBelowMinimum();
    }

    @Transactional
    public Ingredient create(IngredientRequest request) {
        return ingredientRepository.save(Ingredient.builder()
                .name(request.name())
                .unit(request.unit())
                .stockQuantity(request.stockQuantity())
                .minStock(request.minStock())
                .build());
    }

    /** Manual inbound (goods received). */
    @Transactional
    public Ingredient registerInbound(UUID ingredientId, BigDecimal quantity, String note) {
        if (quantity.signum() <= 0) {
            throw new BusinessRuleException("Inbound quantity must be positive");
        }
        Ingredient ingredient = require(ingredientId);
        ingredient.setStockQuantity(ingredient.getStockQuantity().add(quantity));
        movementRepository.save(StockMovement.builder()
                .ingredientId(ingredientId)
                .type(StockMovement.Type.INBOUND)
                .quantity(quantity)
                .note(note)
                .build());
        return ingredient;
    }

    @Transactional
    public Ingredient adjust(UUID ingredientId, BigDecimal newQuantity, String note) {
        Ingredient ingredient = require(ingredientId);
        BigDecimal delta = newQuantity.subtract(ingredient.getStockQuantity());
        ingredient.setStockQuantity(newQuantity);
        movementRepository.save(StockMovement.builder()
                .ingredientId(ingredientId)
                .type(StockMovement.Type.ADJUSTMENT)
                .quantity(delta)
                .note(note)
                .build());
        return ingredient;
    }

    /** Estimated consumption when an order is paid; idempotent per order. */
    @Transactional
    public void consumeForOrder(OrderPaidEvent event) {
        if (movementRepository.existsByOrderId(event.orderId())) {
            return;
        }
        for (OrderLine line : event.items()) {
            List<DishRecipe> recipe = recipeRepository.findByDishId(line.dishId());
            for (DishRecipe component : recipe) {
                ingredientRepository.findById(component.getIngredientId()).ifPresent(ingredient -> {
                    BigDecimal consumed = component.getQuantityPerDish()
                            .multiply(BigDecimal.valueOf(line.quantity()));
                    ingredient.setStockQuantity(ingredient.getStockQuantity().subtract(consumed));
                    movementRepository.save(StockMovement.builder()
                            .ingredientId(ingredient.getId())
                            .type(StockMovement.Type.CONSUMPTION)
                            .quantity(consumed.negate())
                            .orderId(event.orderId())
                            .note("Consumo estimado por pedido")
                            .build());
                    if (ingredient.isBelowMinimum()) {
                        log.warn("LOW STOCK alert: ingredient {} at {} {} (min {})",
                                ingredient.getName(), ingredient.getStockQuantity(),
                                ingredient.getUnit(), ingredient.getMinStock());
                    }
                });
            }
        }
    }

    @Transactional(readOnly = true)
    public List<StockMovement> movements(UUID ingredientId) {
        return movementRepository.findByIngredientIdOrderByCreatedAtDesc(ingredientId);
    }

    @Transactional
    public DishRecipe addRecipeComponent(UUID dishId, UUID ingredientId, BigDecimal quantityPerDish) {
        require(ingredientId);
        return recipeRepository.save(DishRecipe.builder()
                .dishId(dishId)
                .ingredientId(ingredientId)
                .quantityPerDish(quantityPerDish)
                .build());
    }

    private Ingredient require(UUID ingredientId) {
        return ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Ingredient", ingredientId));
    }
}
