package com.sabormayor.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        UUID customerId,
        String orderType,
        BigDecimal total,
        List<OrderLine> items,
        Instant occurredAt) {
}
