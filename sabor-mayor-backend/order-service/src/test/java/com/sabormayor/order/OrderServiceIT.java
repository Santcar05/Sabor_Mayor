package com.sabormayor.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import com.sabormayor.common.events.PaymentConfirmedEvent;
import com.sabormayor.order.application.OrderService;
import com.sabormayor.order.domain.OrderStatus;
import com.sabormayor.order.infrastructure.MenuClient;
import com.sabormayor.order.infrastructure.OutboxRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "app.kafka-listeners-enabled=false",
        "app.outbox.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=false",
        "management.tracing.enabled=false"
})
class OrderServiceIT {

    static final String TABLE_1 = "qr-mesa-01";
    static final UUID CEVICHE = UUID.fromString("20000000-0000-0000-0000-000000000001");
    static final UUID VACIO = UUID.fromString("20000000-0000-0000-0000-000000000005");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderService orderService;

    @Autowired
    OutboxRepository outboxRepository;

    @MockBean
    MenuClient menuClient;

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        when(menuClient.getDishesByIds(anyList())).thenReturn(List.of(
                new MenuClient.MenuDish(CEVICHE, "Ceviche de Maracuya Molecular",
                        "ceviche-de-maracuya-molecular", new BigDecimal("42000.00"), true, "entradas",
                        Set.of("signature")),
                new MenuClient.MenuDish(VACIO, "Vacio en Madera de Olivo",
                        "vacio-en-madera-de-olivo", new BigDecimal("89000.00"), true, "fuertes",
                        Set.of("signature"))));
    }

    private RequestPostProcessor asCustomer() {
        return jwt().jwt(j -> j.subject(customerId.toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asRole(String role) {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private String placeDineInOrder() throws Exception {
        String body = mockMvc.perform(post("/api/orders").with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"DINE_IN","tableId":"%s",
                                 "items":[{"dishId":"%s","quantity":2},{"dishId":"%s","quantity":1}]}
                                """.formatted(TABLE_1, CEVICHE, VACIO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.subtotal").value(173000.0))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void fullDineInLifecycle_kitchenFlow_andOutbox() throws Exception {
        String orderId = placeDineInOrder();

        // Items got routed to stations from the menu category
        String orderJson = mockMvc.perform(get("/api/orders/{id}", orderId).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].station").value("FRIOS"))
                .andExpect(jsonPath("$.items[1].station").value("CALIENTES"))
                .andReturn().getResponse().getContentAsString();
        String hotItemId = JsonPath.read(orderJson, "$.items[1].id");

        // Table became occupied
        mockMvc.perform(get("/api/tables").with(asRole("MESERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == 1)].status").value("OCUPADA"));

        // Waiter confirms
        mockMvc.perform(patch("/api/orders/{id}/status", orderId).with(asRole("MESERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk());

        // Cook starts an item -> order moves to IN_KITCHEN automatically
        mockMvc.perform(patch("/api/kitchen/items/{id}/status", hotItemId).with(asRole("COCINERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_KITCHEN"));

        // KDS shows the order in the CALIENTES station queue
        mockMvc.perform(get("/api/kitchen/orders?station=CALIENTES").with(asRole("COCINERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(orderId)).exists());

        // Invalid transition is rejected by the state machine
        mockMvc.perform(patch("/api/orders/{id}/status", orderId).with(asRole("MESERO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isUnprocessableEntity());

        // Split the bill in 3
        mockMvc.perform(get("/api/orders/{id}/split?parts=3", orderId).with(asRole("MESERO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parts").value(3));

        // Payment confirmed event arrives -> PAID + OrderPaid in the outbox
        orderService.markPaid(new PaymentConfirmedEvent(
                UUID.randomUUID(), UUID.fromString(orderId), customerId,
                new BigDecimal("173000.00"), new BigDecimal("17300.00"), "MOCK", Instant.now()));

        mockMvc.perform(get("/api/orders/{id}", orderId).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.tip").value(17300.0));

        var eventTypes = outboxRepository.findAll().stream()
                .filter(e -> e.getAggregateId().toString().equals(orderId))
                .map(e -> e.getEventType()).toList();
        assertThat(eventTypes)
                .contains("OrderPlacedEvent", "OrderStatusChangedEvent", "OrderPaidEvent");
    }

    @Test
    void cartFlow_andCustomerCancellation() throws Exception {
        mockMvc.perform(post("/api/cart/items").with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dishId\":\"%s\",\"quantity\":1}".formatted(CEVICHE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(1)));

        String body = mockMvc.perform(post("/api/orders").with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PICKUP\",\"fromCart\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(42000.0))
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(body, "$.id");

        // Cart was cleared after placing the order
        mockMvc.perform(get("/api/cart").with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", org.hamcrest.Matchers.hasSize(0)));

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deliveryRequiresAddress_andRbacOnKitchen() throws Exception {
        mockMvc.perform(post("/api/orders").with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DELIVERY\",\"items\":[{\"dishId\":\"%s\",\"quantity\":1}]}"
                                .formatted(CEVICHE)))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/kitchen/orders").with(asCustomer()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tables/by-qr/qr-mesa-02").with(asCustomer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2));
    }
}
