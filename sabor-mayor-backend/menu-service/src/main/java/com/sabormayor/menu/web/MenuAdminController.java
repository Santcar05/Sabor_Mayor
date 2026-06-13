package com.sabormayor.menu.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.menu.application.MenuService;
import com.sabormayor.menu.domain.DishPriceHistory;
import com.sabormayor.menu.web.dto.CategoryRequest;
import com.sabormayor.menu.web.dto.CategoryResponse;
import com.sabormayor.menu.web.dto.DishMarginResponse;
import com.sabormayor.menu.web.dto.DishRequest;
import com.sabormayor.menu.web.dto.DishResponse;
import com.sabormayor.menu.web.dto.UpdatePriceRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Menu (admin)")
public class MenuAdminController {

    private final MenuService menuService;

    @PostMapping("/dishes")
    public ResponseEntity<DishResponse> createDish(@Valid @RequestBody DishRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createDish(request));
    }

    @PutMapping("/dishes/{dishId}")
    public DishResponse updateDish(@PathVariable UUID dishId, @Valid @RequestBody DishRequest request) {
        return menuService.updateDish(dishId, request);
    }

    @PatchMapping("/dishes/{dishId}/availability")
    public DishResponse updateAvailability(@PathVariable UUID dishId, @RequestParam boolean available) {
        return menuService.updateAvailability(dishId, available);
    }

    @PatchMapping("/dishes/{dishId}/price")
    @Operation(summary = "Change price keeping full price history")
    public DishResponse updatePrice(@PathVariable UUID dishId, @Valid @RequestBody UpdatePriceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return menuService.updatePrice(dishId, request.price(), UUID.fromString(jwt.getSubject()));
    }

    @DeleteMapping("/dishes/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable UUID dishId) {
        menuService.deleteDish(dishId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dishes/{dishId}/price-history")
    public List<DishPriceHistory> priceHistory(@PathVariable UUID dishId) {
        return menuService.getPriceHistory(dishId);
    }

    @GetMapping("/margins")
    @Operation(summary = "Price/cost/margin report per dish")
    public List<DishMarginResponse> margins() {
        return menuService.getMargins();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    public CategoryResponse updateCategory(@PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return menuService.updateCategory(categoryId, request);
    }
}
