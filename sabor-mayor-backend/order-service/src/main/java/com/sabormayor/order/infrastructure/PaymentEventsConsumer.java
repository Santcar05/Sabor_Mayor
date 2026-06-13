package com.sabormayor.order.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.PaymentConfirmedEvent;
import com.sabormayor.common.events.PaymentRefundedEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.order.application.OrderService;

import lombok.RequiredArgsConstructor;

@Component
@KafkaListener(topics = KafkaTopics.PAYMENTS_EVENTS, groupId = "order-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class PaymentEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventsConsumer.class);

    private final OrderService orderService;

    @KafkaHandler
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("PaymentConfirmed received for order {}", event.orderId());
        orderService.markPaid(event);
    }

    @KafkaHandler
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        log.info("PaymentRefunded received for order {} (partial={})", event.orderId(), event.partial());
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
