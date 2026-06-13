package com.sabormayor.reservation.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.sabormayor.reservation.domain.ReservationStatus;

public record ReservationResponse(
        UUID id,
        UUID customerId,
        String customerName,
        LocalDate date,
        LocalTime time,
        int partySize,
        ReservationStatus status,
        boolean depositRequired,
        BigDecimal depositAmount,
        String specialRequests,
        List<PreOrderItemResponse> preOrderItems,
        Instant createdAt) {

    public record PreOrderItemResponse(UUID dishId, int quantity) {
    }
}
