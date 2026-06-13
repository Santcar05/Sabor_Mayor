package com.sabormayor.reservation.web.dto;

import java.time.LocalTime;

public record AvailabilitySlot(
        LocalTime time,
        int remainingSeats,
        boolean fitsParty) {
}
