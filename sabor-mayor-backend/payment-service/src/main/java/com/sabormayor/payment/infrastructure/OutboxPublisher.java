package com.sabormayor.payment.infrastructure;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabormayor.common.events.PaymentConfirmedEvent;
import com.sabormayor.common.events.PaymentRefundedEvent;
import com.sabormayor.payment.domain.OutboxEvent;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "app.outbox.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private static final Map<String, Class<?>> EVENT_TYPES = Map.of(
            PaymentConfirmedEvent.class.getSimpleName(), PaymentConfirmedEvent.class,
            PaymentRefundedEvent.class.getSimpleName(), PaymentRefundedEvent.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : outboxRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                Class<?> type = EVENT_TYPES.get(event.getEventType());
                if (type == null) {
                    log.error("Unknown outbox event type {}, skipping {}", event.getEventType(), event.getId());
                    continue;
                }
                Object payload = objectMapper.readValue(event.getPayload(), type);
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), payload).get();
                event.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                log.warn("Could not publish outbox event {} ({}), will retry: {}",
                        event.getId(), event.getEventType(), ex.getMessage());
                return;
            }
        }
    }
}
