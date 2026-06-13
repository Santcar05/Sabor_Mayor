package com.sabormayor.common.events;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        UUID customerId,
        String previousStatus,
        String newStatus,
        Instant occurredAt) {
}
