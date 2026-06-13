package com.sabormayor.inventory.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IngredientRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 20) String unit,
        @NotNull @DecimalMin("0.0") BigDecimal stockQuantity,
        @NotNull @DecimalMin("0.0") BigDecimal minStock) {
}
