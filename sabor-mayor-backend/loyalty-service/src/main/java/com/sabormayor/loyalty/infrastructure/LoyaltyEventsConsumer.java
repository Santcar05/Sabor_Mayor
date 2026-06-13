package com.sabormayor.loyalty.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.common.security.Roles;
import com.sabormayor.loyalty.application.LoyaltyService;

import lombok.RequiredArgsConstructor;

@Component
@KafkaListener(topics = {KafkaTopics.ORDERS_EVENTS, KafkaTopics.USERS_EVENTS}, groupId = "loyalty-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class LoyaltyEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyEventsConsumer.class);

    private final LoyaltyService loyaltyService;

    @KafkaHandler
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("OrderPaid received, crediting points for customer {}", event.customerId());
        loyaltyService.earnFromOrder(event);
    }

    @KafkaHandler
    public void onUserRegistered(UserRegisteredEvent event) {
        if (Roles.CLIENTE.equals(event.role())) {
            loyaltyService.getOrCreateAccount(event.userId());
        }
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
