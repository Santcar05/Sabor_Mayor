package com.sabormayor.reservation.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.common.security.Roles;
import com.sabormayor.reservation.application.ReservationService;
import com.sabormayor.reservation.domain.ReservationStatus;
import com.sabormayor.reservation.mapper.ReservationMapper;
import com.sabormayor.reservation.web.dto.AvailabilitySlot;
import com.sabormayor.reservation.web.dto.CreateReservationRequest;
import com.sabormayor.reservation.web.dto.ReservationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper mapper;

    @GetMapping("/availability")
    @Operation(summary = "Available time slots and remaining seats for a date (public)")
    public List<AvailabilitySlot> availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "2") int partySize) {
        return reservationService.getAvailability(date, partySize);
    }

    @PostMapping
    @Operation(summary = "Create a reservation; groups of 8+ get a deposit requirement")
    public ResponseEntity<ReservationResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateReservationRequest request) {
        var reservation = reservationService.create(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : "Cliente",
                request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(reservation));
    }

    @GetMapping("/me")
    public List<ReservationResponse> myReservations(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponses(reservationService.myReservations(UUID.fromString(jwt.getSubject())));
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation (owner or staff)")
    public ReservationResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID reservationId) {
        String role = jwt.getClaimAsString("role");
        boolean isStaff = role != null && !Roles.CLIENTE.equals(role);
        return mapper.toResponse(reservationService.cancel(
                reservationId, UUID.fromString(jwt.getSubject()), isStaff));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESERO','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Reservations of a given day (staff)")
    public List<ReservationResponse> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return mapper.toResponses(reservationService.byDate(date));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "All reservations without date filter (admin)")
    public List<ReservationResponse> allReservations() {
        return mapper.toResponses(reservationService.allReservations());
    }

    @PatchMapping("/{reservationId}/status")
    @PreAuthorize("hasAnyRole('MESERO','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Confirm / complete / mark no-show (staff)")
    public ReservationResponse updateStatus(@PathVariable UUID reservationId,
            @RequestParam @NotNull ReservationStatus status) {
        return mapper.toResponse(reservationService.updateStatus(reservationId, status));
    }

    @PostMapping("/blocked-dates")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Block a full date (holidays, private events)")
    public ResponseEntity<Void> blockDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String reason) {
        reservationService.blockDate(date, reason);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
