package com.sabormayor.reservation.application;

import java.time.Instant;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.events.ReservationReminderDueEvent;
import com.sabormayor.reservation.infrastructure.ReservationEventPublisher;
import com.sabormayor.reservation.infrastructure.ReservationRepository;

import lombok.RequiredArgsConstructor;

/** Publishes a reminder event for reservations happening tomorrow (~24h ahead). */
@Component
@ConditionalOnProperty(name = "app.reminders.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher eventPublisher;

    @Scheduled(fixedDelayString = "${app.reminders.poll-interval-ms:3600000}")
    @Transactional
    public void sendDueReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        reservationRepository.findReminderCandidates(tomorrow).forEach(reservation -> {
            log.info("Publishing 24h reminder for reservation {}", reservation.getId());
            eventPublisher.publishReminderDue(new ReservationReminderDueEvent(
                    reservation.getId(), reservation.getCustomerId(), reservation.getCustomerEmail(),
                    reservation.getCustomerName(), reservation.getDate(), reservation.getTime(),
                    reservation.getPartySize(), Instant.now()));
            reservation.setReminderSentAt(Instant.now());
        });
    }
}
