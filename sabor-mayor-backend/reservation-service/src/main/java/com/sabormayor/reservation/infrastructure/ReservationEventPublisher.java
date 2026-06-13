package com.sabormayor.reservation.infrastructure;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.ReservationCancelledEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;
import com.sabormayor.common.events.ReservationReminderDueEvent;
import com.sabormayor.common.kafka.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(ReservationCreatedEvent event) {
        kafkaTemplate.send(KafkaTopics.RESERVATIONS_EVENTS, event.reservationId().toString(), event);
    }

    public void publishCancelled(ReservationCancelledEvent event) {
        kafkaTemplate.send(KafkaTopics.RESERVATIONS_EVENTS, event.reservationId().toString(), event);
    }

    public void publishReminderDue(ReservationReminderDueEvent event) {
        kafkaTemplate.send(KafkaTopics.RESERVATIONS_EVENTS, event.reservationId().toString(), event);
    }
}
