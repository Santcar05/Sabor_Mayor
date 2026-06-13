package com.sabormayor.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID paymentId,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        BigDecimal tip,
        String method,
        Instant occurredAt) {
}
