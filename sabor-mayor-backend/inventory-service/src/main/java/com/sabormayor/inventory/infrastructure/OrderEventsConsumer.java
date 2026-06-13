package com.sabormayor.inventory.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.inventory.application.InventoryService;

import lombok.RequiredArgsConstructor;

@Component
@KafkaListener(topics = KafkaTopics.ORDERS_EVENTS, groupId = "inventory-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumer.class);

    private final InventoryService inventoryService;

    @KafkaHandler
    public void onOrderPaid(OrderPaidEvent event) {
        log.info("OrderPaid received, estimating consumption for order {}", event.orderId());
        inventoryService.consumeForOrder(event);
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
