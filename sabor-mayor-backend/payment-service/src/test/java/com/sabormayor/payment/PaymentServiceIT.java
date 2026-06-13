package com.sabormayor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.jayway.jsonpath.JsonPath;
import com.sabormayor.payment.infrastructure.OutboxRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "app.outbox.enabled=false",
        "management.tracing.enabled=false"
})
class PaymentServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OutboxRepository outboxRepository;

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    private RequestPostProcessor asCustomer(UUID id) {
        return jwt().jwt(j -> j.subject(id.toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void chargeWithTip_thenPartialAndFinalRefund() throws Exception {
        UUID customer = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        String body = mockMvc.perform(post("/api/payments").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","amount":100000,"tip":10000,"method":"CARD",
                                 "paymentMethodToken":"tok_visa"}
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.gateway").value("MOCK"))
                .andReturn().getResponse().getContentAsString();
        String paymentId = JsonPath.read(body, "$.id");

        // PaymentConfirmed landed in the outbox
        assertThat(outboxRepository.findAll())
                .anyMatch(e -> e.getEventType().equals("PaymentConfirmedEvent"));

        // Partial refund
        mockMvc.perform(post("/api/payments/{id}/refund", paymentId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":30000,\"reason\":\"Plato devuelto\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_REFUNDED"))
                .andExpect(jsonPath("$.refundedAmount").value(30000.0));

        // Refund exceeding the remaining amount is rejected
        mockMvc.perform(post("/api/payments/{id}/refund", paymentId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":999999}"))
                .andExpect(status().isUnprocessableEntity());

        // Full refund of the remainder
        mockMvc.perform(post("/api/payments/{id}/refund", paymentId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.refundedAmount").value(110000.0));

        assertThat(outboxRepository.findAll())
                .filteredOn(e -> e.getEventType().equals("PaymentRefundedEvent"))
                .hasSize(2);
    }

    @Test
    void declinedCardReturns422ButKeepsAuditRecord() throws Exception {
        UUID customer = UUID.randomUUID();

        mockMvc.perform(post("/api/payments").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","amount":50000,"method":"CARD","paymentMethodToken":"tok_fail"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/payments/me").with(asCustomer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FAILED"));
    }

    @Test
    void refundRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/payments/{id}/refund", UUID.randomUUID())
                        .with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
