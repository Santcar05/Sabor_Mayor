package com.sabormayor.order.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CartItemRequest(
        @NotNull UUID dishId,
        @Min(1) @Max(50) int quantity,
        @Size(max = 300) String notes) {
}
