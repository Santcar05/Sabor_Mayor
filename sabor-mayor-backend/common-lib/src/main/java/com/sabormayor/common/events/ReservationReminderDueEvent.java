package com.sabormayor.common.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Emitted ~24h before the reservation so notification-service sends a reminder. */
public record ReservationReminderDueEvent(
        UUID reservationId,
        UUID customerId,
        String customerEmail,
        String customerName,
        LocalDate date,
        LocalTime time,
        int partySize,
        Instant occurredAt) {
}
