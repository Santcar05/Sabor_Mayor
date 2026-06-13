package com.sabormayor.analytics.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.analytics.application.AnalyticsService;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.ReservationCancelledEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;
import com.sabormayor.common.kafka.KafkaTopics;

import lombok.RequiredArgsConstructor;

/** Builds the read models from sales and reservation events. */
@Component
@KafkaListener(topics = {KafkaTopics.ORDERS_EVENTS, KafkaTopics.RESERVATIONS_EVENTS},
        groupId = "analytics-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class DomainEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DomainEventsConsumer.class);

    private final AnalyticsService analyticsService;

    @KafkaHandler
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("OrderPaid received, updating sales read model for order {}", event.orderId());
        analyticsService.recordSale(event);
    }

    @KafkaHandler
    public void onReservationCreated(ReservationCreatedEvent event) {
        analyticsService.recordReservation(event);
    }

    @KafkaHandler
    public void onReservationCancelled(ReservationCancelledEvent event) {
        analyticsService.markReservationCancelled(event.reservationId());
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
