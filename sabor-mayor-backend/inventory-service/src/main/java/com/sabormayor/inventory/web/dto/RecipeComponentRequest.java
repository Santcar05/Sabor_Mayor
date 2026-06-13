package com.sabormayor.inventory.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RecipeComponentRequest(
        @NotNull UUID dishId,
        @NotNull UUID ingredientId,
        @NotNull @DecimalMin("0.001") BigDecimal quantityPerDish) {
}
