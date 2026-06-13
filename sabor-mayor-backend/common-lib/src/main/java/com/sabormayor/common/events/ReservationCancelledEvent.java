package com.sabormayor.common.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationCancelledEvent(
        UUID reservationId,
        UUID customerId,
        String customerEmail,
        LocalDate date,
        LocalTime time,
        Instant occurredAt) {
}
