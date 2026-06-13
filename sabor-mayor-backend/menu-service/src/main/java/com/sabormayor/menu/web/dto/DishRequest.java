package com.sabormayor.menu.web.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DishRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @DecimalMin("0.0") BigDecimal cost,
        boolean available,
        boolean featured,
        String imageUrl,
        Integer prepMinutes,
        Set<String> tags,
        Set<String> allergens,
        Set<UUID> pairingIds) {
}
