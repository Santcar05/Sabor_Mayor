package com.sabormayor.order.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitResponse(
        UUID orderId,
        int parts,
        BigDecimal amountPerPart,
        BigDecimal firstPartAmount,
        BigDecimal total) {
}
