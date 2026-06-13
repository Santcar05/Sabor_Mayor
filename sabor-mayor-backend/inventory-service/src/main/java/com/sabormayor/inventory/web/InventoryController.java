package com.sabormayor.inventory.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.inventory.application.InventoryService;
import com.sabormayor.inventory.domain.DishRecipe;
import com.sabormayor.inventory.domain.Ingredient;
import com.sabormayor.inventory.domain.StockMovement;
import com.sabormayor.inventory.web.dto.IngredientRequest;
import com.sabormayor.inventory.web.dto.RecipeComponentRequest;
import com.sabormayor.inventory.web.dto.StockChangeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Inventory is staff-only (cooks and admins). */
@RestController
@RequestMapping("/api/inventory")
@PreAuthorize("hasAnyRole('COCINERO','ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/ingredients")
    public List<Ingredient> ingredients() {
        return inventoryService.findAll();
    }

    @GetMapping("/ingredients/low-stock")
    @Operation(summary = "Ingredients at or below their minimum stock")
    public List<Ingredient> lowStock() {
        return inventoryService.lowStock();
    }

    @PostMapping("/ingredients")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Ingredient> create(@Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @PostMapping("/ingredients/{id}/inbound")
    @Operation(summary = "Register received goods")
    public Ingredient inbound(@PathVariable UUID id, @Valid @RequestBody StockChangeRequest request) {
        return inventoryService.registerInbound(id, request.quantity(), request.note());
    }

    @PutMapping("/ingredients/{id}/adjust")
    @Operation(summary = "Set the stock to an absolute value (stock-take)")
    public Ingredient adjust(@PathVariable UUID id, @Valid @RequestBody StockChangeRequest request) {
        return inventoryService.adjust(id, request.quantity(), request.note());
    }

    @GetMapping("/ingredients/{id}/movements")
    public List<StockMovement> movements(@PathVariable UUID id) {
        return inventoryService.movements(id);
    }

    @PostMapping("/recipes")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Link a dish to an ingredient and its per-dish consumption")
    public ResponseEntity<DishRecipe> addRecipe(@Valid @RequestBody RecipeComponentRequest request) {
        DishRecipe recipe = inventoryService.addRecipeComponent(
                request.dishId(), request.ingredientId(), request.quantityPerDish());
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }
}
