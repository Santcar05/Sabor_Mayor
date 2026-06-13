package com.sabormayor.user.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.UserDeletedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.user.application.UserSyncService;

import lombok.RequiredArgsConstructor;

/** Replicates the auth-service user read model into this service's database. */
@Component
@KafkaListener(topics = KafkaTopics.USERS_EVENTS, groupId = "user-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class UserEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventsConsumer.class);

    private final UserSyncService userSyncService;

    @KafkaHandler
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("UserRegistered received: {} ({})", event.email(), event.role());
        userSyncService.onUserRegistered(event);
    }

    @KafkaHandler
    public void onUserDeleted(UserDeletedEvent event) {
        log.info("UserDeleted received: {}", event.userId());
        userSyncService.onUserDeleted(event);
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
