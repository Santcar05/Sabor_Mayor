package com.sabormayor.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundedEvent(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        boolean partial,
        Instant occurredAt) {
}
