package com.sabormayor.menu.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DishMarginResponse(
        UUID id,
        String name,
        BigDecimal price,
        BigDecimal cost,
        BigDecimal marginPercent) {
}
