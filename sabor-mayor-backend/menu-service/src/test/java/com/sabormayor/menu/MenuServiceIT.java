package com.sabormayor.menu;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "management.tracing.enabled=false"
})
class MenuServiceIT {

    static final String VACIO_ID = "20000000-0000-0000-0000-000000000005";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    MockMvc mockMvc;

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void publicMenuIsReadableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(5)));

        mockMvc.perform(get("/api/menu/dishes?category=entradas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categorySlug", Matchers.everyItem(Matchers.is("entradas"))));

        mockMvc.perform(get("/api/menu/dishes/ceviche-ancestral"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ceviche Ancestral"))
                .andExpect(jsonPath("$.allergens[0]").value("pescado"));
    }

    @Test
    void adminWriteEvictsCacheSoPublicMenuSeesTheChange() throws Exception {
        // Warm the cache
        mockMvc.perform(get("/api/menu/dishes/vacio-en-madera-de-olivo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(89000.0));

        // Admin changes the price (recorded in history, evicts cache)
        mockMvc.perform(patch("/api/menu/admin/dishes/{id}/price", VACIO_ID).with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":95000}"))
                .andExpect(status().isOk());

        // Public read must reflect the new price (cache was evicted)
        mockMvc.perform(get("/api/menu/dishes/vacio-en-madera-de-olivo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(95000.0));

        mockMvc.perform(get("/api/menu/admin/dishes/{id}/price-history", VACIO_ID).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(2))));
    }

    @Test
    void writeEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(post("/api/menu/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Especiales\",\"displayOrder\":9}"))
                .andExpect(status().isUnauthorized());

        RequestPostProcessor asCustomer = jwt()
                .jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        mockMvc.perform(post("/api/menu/admin/categories").with(asCustomer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Especiales\",\"displayOrder\":9}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/menu/admin/categories").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Especiales\",\"displayOrder\":9}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("especiales"));
    }

    @Test
    void marginReportComputesPercentage() throws Exception {
        mockMvc.perform(get("/api/menu/admin/margins").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].marginPercent", Matchers.hasItem(Matchers.notNullValue())));
    }
}
