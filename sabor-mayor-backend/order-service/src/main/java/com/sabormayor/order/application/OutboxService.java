package com.sabormayor.order.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabormayor.order.domain.OutboxEvent;
import com.sabormayor.order.infrastructure.OutboxRepository;

import lombok.RequiredArgsConstructor;

/** Writes domain events to the outbox inside the caller's transaction. */
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void append(String topic, UUID aggregateId, Object event) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(aggregateId)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .topic(topic)
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize outbox event", e);
        }
    }
}
