package com.sabormayor.auth.infrastructure;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.UserDeletedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.common.kafka.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(KafkaTopics.USERS_EVENTS, event.userId().toString(), event);
    }

    public void publishUserDeleted(UserDeletedEvent event) {
        kafkaTemplate.send(KafkaTopics.USERS_EVENTS, event.userId().toString(), event);
    }
}
