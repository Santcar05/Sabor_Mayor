package com.sabormayor.inventory;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
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

import com.sabormayor.common.events.OrderLine;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.inventory.application.InventoryService;

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
class InventoryServiceIT {

    static final UUID CEVICHE = UUID.fromString("20000000-0000-0000-0000-000000000001");
    static final UUID PESCADO = UUID.fromString("60000000-0000-0000-0000-000000000001");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InventoryService inventoryService;

    private RequestPostProcessor asCook() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "COCINERO"))
                .authorities(new SimpleGrantedAuthority("ROLE_COCINERO"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void orderPaidConsumesIngredientsViaRecipe_idempotently() throws Exception {
        UUID orderId = UUID.randomUUID();
        // 3 ceviches -> 3 * 0.200 = 0.600 kg pescado from a starting 25.000
        OrderPaidEvent event = new OrderPaidEvent(orderId, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("126000"), BigDecimal.ZERO,
                List.of(new OrderLine(CEVICHE, "Ceviche", 3, new BigDecimal("42000"))), Instant.now());

        inventoryService.consumeForOrder(event);
        inventoryService.consumeForOrder(event); // duplicate must be ignored

        mockMvc.perform(get("/api/inventory/ingredients/{id}/movements", PESCADO).with(asCook()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("CONSUMPTION"));
    }

    @Test
    void inboundAndLowStockAlerting() throws Exception {
        // Drop a fresh ingredient below its minimum, then check the low-stock list
        String body = mockMvc.perform(post("/api/inventory/ingredients").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Aji limo\",\"unit\":\"kg\",\"stockQuantity\":1,\"minStock\":2}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ingredientId = com.jayway.jsonpath.JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/inventory/ingredients/low-stock").with(asCook()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Aji limo')]").exists());

        // Inbound restock lifts it above minimum
        mockMvc.perform(post("/api/inventory/ingredients/{id}/inbound", ingredientId).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5,\"note\":\"Compra semanal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(6.0));
    }

    @Test
    void customersCannotAccessInventory() throws Exception {
        RequestPostProcessor asCustomer = jwt()
                .jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        mockMvc.perform(get("/api/inventory/ingredients").with(asCustomer))
                .andExpect(status().isForbidden());
    }
}
