package com.sabormayor.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        UUID customerId,
        UUID paymentId,
        BigDecimal total,
        BigDecimal tip,
        List<OrderLine> items,
        Instant occurredAt) {
}
