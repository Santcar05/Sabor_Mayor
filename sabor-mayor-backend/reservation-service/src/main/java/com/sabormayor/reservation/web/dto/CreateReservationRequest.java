package com.sabormayor.reservation.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReservationRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @Min(1) @Max(30) int partySize,
        @Size(max = 500) String specialRequests,
        @Valid List<PreOrderItemRequest> preOrderItems) {

    public record PreOrderItemRequest(@NotNull UUID dishId, @Min(1) @Max(50) int quantity) {
    }
}
