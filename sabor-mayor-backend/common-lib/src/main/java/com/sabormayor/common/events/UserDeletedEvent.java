package com.sabormayor.common.events;

import java.time.Instant;
import java.util.UUID;

/** Habeas Data: emitted when a customer requests account/data deletion. */
public record UserDeletedEvent(
        UUID userId,
        String email,
        Instant occurredAt) {
}
