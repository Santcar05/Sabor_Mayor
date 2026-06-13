package com.sabormayor.common.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationCreatedEvent(
        UUID reservationId,
        UUID customerId,
        String customerEmail,
        String customerName,
        LocalDate date,
        LocalTime time,
        int partySize,
        Instant occurredAt) {
}
