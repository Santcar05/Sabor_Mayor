package com.sabormayor.reservation.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.BusinessRuleException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.common.events.ReservationCancelledEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;
import com.sabormayor.reservation.domain.BlockedDate;
import com.sabormayor.reservation.domain.Reservation;
import com.sabormayor.reservation.domain.ReservationStatus;
import com.sabormayor.reservation.domain.ScheduleRule;
import com.sabormayor.reservation.infrastructure.BlockedDateRepository;
import com.sabormayor.reservation.infrastructure.ReservationEventPublisher;
import com.sabormayor.reservation.infrastructure.ReservationRepository;
import com.sabormayor.reservation.infrastructure.ScheduleRuleRepository;
import com.sabormayor.reservation.web.dto.AvailabilitySlot;
import com.sabormayor.reservation.web.dto.CreateReservationRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    /** Groups of 8+ must leave a deposit (per person). */
    public static final int DEPOSIT_PARTY_SIZE = 8;
    public static final BigDecimal DEPOSIT_PER_PERSON = new BigDecimal("30000");

    private final ReservationRepository reservationRepository;
    private final ScheduleRuleRepository scheduleRuleRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final ReservationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> getAvailability(LocalDate date, int partySize) {
        if (blockedDateRepository.existsByDate(date)) {
            return List.of();
        }
        ScheduleRule rule = scheduleRuleRepository.findByDayOfWeek(date.getDayOfWeek())
                .orElse(null);
        if (rule == null) {
            return List.of();
        }
        List<AvailabilitySlot> slots = new ArrayList<>();
        for (LocalTime t = rule.getOpenTime(); t.isBefore(rule.getCloseTime());
                t = t.plusMinutes(rule.getSlotMinutes())) {
            int remaining = rule.getCapacityPerSlot() - reservationRepository.bookedSeats(date, t);
            slots.add(new AvailabilitySlot(t, Math.max(remaining, 0), remaining >= partySize));
        }
        return slots;
    }

    @Transactional
    public Reservation create(UUID customerId, String email, String name, CreateReservationRequest request) {
        if (request.date().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Reservation date must be in the future");
        }
        if (blockedDateRepository.existsByDate(request.date())) {
            throw new BusinessRuleException("The restaurant is not taking reservations on that date");
        }
        ScheduleRule rule = scheduleRuleRepository.findByDayOfWeek(request.date().getDayOfWeek())
                .orElseThrow(() -> new BusinessRuleException("The restaurant is closed that day"));
        if (request.time().isBefore(rule.getOpenTime()) || !request.time().isBefore(rule.getCloseTime())) {
            throw new BusinessRuleException("Requested time is outside opening hours");
        }
        int remaining = rule.getCapacityPerSlot() - reservationRepository.bookedSeats(request.date(), request.time());
        if (remaining < request.partySize()) {
            throw new BusinessRuleException("Not enough capacity for that time slot");
        }

        boolean depositRequired = request.partySize() >= DEPOSIT_PARTY_SIZE;
        Reservation reservation = Reservation.builder()
                .customerId(customerId)
                .customerName(name)
                .customerEmail(email)
                .date(request.date())
                .time(request.time())
                .partySize(request.partySize())
                .status(ReservationStatus.PENDING)
                .depositRequired(depositRequired)
                .depositAmount(depositRequired
                        ? DEPOSIT_PER_PERSON.multiply(BigDecimal.valueOf(request.partySize()))
                        : null)
                .specialRequests(request.specialRequests())
                .build();
        if (request.preOrderItems() != null) {
            request.preOrderItems().forEach(i -> reservation.getPreOrderItems()
                    .add(new Reservation.PreOrderItem(i.dishId(), i.quantity())));
        }
        Reservation saved = reservationRepository.save(reservation);

        eventPublisher.publishCreated(new ReservationCreatedEvent(
                saved.getId(), customerId, email, name, saved.getDate(), saved.getTime(),
                saved.getPartySize(), Instant.now()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Reservation> myReservations(UUID customerId) {
        return reservationRepository.findByCustomerIdOrderByDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> byDate(LocalDate date) {
        return reservationRepository.findByDateOrderByTimeAsc(date);
    }

    @Transactional
    public Reservation cancel(UUID reservationId, UUID requesterId, boolean isStaff) {
        Reservation reservation = require(reservationId);
        if (!isStaff && !reservation.getCustomerId().equals(requesterId)) {
            throw ResourceNotFoundException.of("Reservation", reservationId);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessRuleException("Reservation can no longer be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        eventPublisher.publishCancelled(new ReservationCancelledEvent(
                reservation.getId(), reservation.getCustomerId(), reservation.getCustomerEmail(),
                reservation.getDate(), reservation.getTime(), Instant.now()));
        return reservation;
    }

    @Transactional
    public Reservation updateStatus(UUID reservationId, ReservationStatus status) {
        Reservation reservation = require(reservationId);
        reservation.setStatus(status);
        return reservation;
    }

    @Transactional
    public BlockedDate blockDate(LocalDate date, String reason) {
        if (blockedDateRepository.existsByDate(date)) {
            throw new BusinessRuleException("Date is already blocked");
        }
        return blockedDateRepository.save(BlockedDate.builder().date(date).reason(reason).build());
    }

    private Reservation require(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", reservationId));
    }
}
