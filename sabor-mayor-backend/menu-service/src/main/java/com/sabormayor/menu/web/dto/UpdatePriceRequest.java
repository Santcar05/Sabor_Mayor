package com.sabormayor.menu.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdatePriceRequest(@NotNull @DecimalMin("0.01") BigDecimal price) {
}
