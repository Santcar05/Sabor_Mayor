package com.sabormayor.menu.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.menu.application.MenuService;
import com.sabormayor.menu.web.dto.CategoryResponse;
import com.sabormayor.menu.web.dto.DishResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** Public, Redis-cached menu endpoints consumed by the storefront. */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menu (public)")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    @Operation(summary = "All categories ordered for display")
    public List<CategoryResponse> categories() {
        return menuService.getCategories();
    }

    @GetMapping("/dishes")
    @Operation(summary = "Dishes, optionally filtered by category slug and/or tag")
    public List<DishResponse> dishes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "true") boolean onlyAvailable) {
        return menuService.getDishes(category, tag, onlyAvailable);
    }

    @GetMapping("/dishes/{slug}")
    @Operation(summary = "Dish detail by slug")
    public DishResponse dish(@PathVariable String slug) {
        return menuService.getDishBySlug(slug);
    }

    @GetMapping("/dishes/by-ids")
    @Operation(summary = "Batch lookup by ids (used by order-service to validate prices)")
    public List<DishResponse> dishesByIds(@RequestParam java.util.List<java.util.UUID> ids) {
        return menuService.getDishesByIds(ids);
    }
}
