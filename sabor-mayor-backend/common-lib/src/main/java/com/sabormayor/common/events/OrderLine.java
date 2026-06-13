package com.sabormayor.common.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLine(
        UUID dishId,
        String dishName,
        int quantity,
        BigDecimal unitPrice) {
}
