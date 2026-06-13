package com.sabormayor.notification;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sabormayor.common.events.ReservationCreatedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.notification.infrastructure.DomainEventsConsumer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "app.kafka-listeners-enabled=false",
        "management.tracing.enabled=false"
})
class NotificationServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DomainEventsConsumer consumer;

    private RequestPostProcessor asUser(UUID id) {
        return jwt().jwt(j -> j.subject(id.toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void eventsProduceLoggedNotifications() throws Exception {
        UUID user = UUID.randomUUID();

        consumer.onUserRegistered(new UserRegisteredEvent(
                user, "nuevo@sabormayor.com", "Nuevo Cliente", "CLIENTE", Instant.now()));
        consumer.onReservationCreated(new ReservationCreatedEvent(
                UUID.randomUUID(), user, "nuevo@sabormayor.com", "Nuevo Cliente",
                LocalDate.now().plusDays(3), LocalTime.of(20, 0), 4, Instant.now()));

        // Welcome email + reservation email + reservation WhatsApp = 3
        mockMvc.perform(get("/api/notifications/me").with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/notifications").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SENT"));
    }

    @Test
    void customerCannotReadGlobalLog() throws Exception {
        mockMvc.perform(get("/api/notifications").with(asUser(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
