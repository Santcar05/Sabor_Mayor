package com.sabormayor.loyalty;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.loyalty.application.LoyaltyService;

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
class LoyaltyServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    LoyaltyService loyaltyService;

    private RequestPostProcessor asUser(UUID id) {
        return jwt().jwt(j -> j.subject(id.toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    @Test
    void orderPaidEventCreditsPointsIdempotently_andRedeemWorks() throws Exception {
        UUID customer = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderPaidEvent event = new OrderPaidEvent(orderId, customer, UUID.randomUUID(),
                new BigDecimal("173000"), new BigDecimal("17300"), List.of(), Instant.now());

        loyaltyService.earnFromOrder(event);
        loyaltyService.earnFromOrder(event); // duplicate delivery must not double-credit

        mockMvc.perform(get("/api/loyalty/me").with(asUser(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(173))
                .andExpect(jsonPath("$.level").value("ALUMNO_CULINARIO"))
                .andExpect(jsonPath("$.pointsToNextLevel").value(327));

        mockMvc.perform(post("/api/loyalty/me/redeem").with(asUser(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(73))
                .andExpect(jsonPath("$.lifetimePoints").value(173));

        // Over-redeeming is rejected
        mockMvc.perform(post("/api/loyalty/me/redeem").with(asUser(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"points\":999}"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/loyalty/me/transactions").with(asUser(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    void levelUpgradesWithLifetimePoints() throws Exception {
        UUID customer = UUID.randomUUID();
        loyaltyService.earnFromOrder(new OrderPaidEvent(UUID.randomUUID(), customer, UUID.randomUUID(),
                new BigDecimal("2500000"), BigDecimal.ZERO, List.of(), Instant.now()));

        mockMvc.perform(get("/api/loyalty/me").with(asUser(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("CHEF_INVITADO"));
    }
}
