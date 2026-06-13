package com.sabormayor.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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

import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.user.application.UserSyncService;

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
class UserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserSyncService userSyncService;

    private RequestPostProcessor asCustomer(UUID userId) {
        return jwt().jwt(j -> j.subject(userId.toString()).claim("email", "c@x.com").claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void profileLifecycleWithAddressesAndPaymentMethods() throws Exception {
        UUID userId = UUID.randomUUID();

        // Simulate the UserRegistered event projection
        userSyncService.onUserRegistered(new UserRegisteredEvent(
                userId, "lifecycle@sabormayor.com", "Cliente Ciclo", "CLIENTE", Instant.now()));

        mockMvc.perform(get("/api/users/me").with(asCustomer(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("lifecycle@sabormayor.com"));

        mockMvc.perform(put("/api/users/me").with(asCustomer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Cliente Actualizado","phone":"3001234567",
                                 "dietaryPreferences":["vegetariano"],"allergies":["mani"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Cliente Actualizado"))
                .andExpect(jsonPath("$.allergies[0]").value("mani"));

        String addressJson = mockMvc.perform(post("/api/users/me/addresses").with(asCustomer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label":"Casa","street":"Calle 1 # 2-3","city":"Bogota","defaultAddress":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.defaultAddress").value(true))
                .andReturn().getResponse().getContentAsString();

        String addressId = com.jayway.jsonpath.JsonPath.read(addressJson, "$.id");

        mockMvc.perform(post("/api/users/me/payment-methods").with(asCustomer(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"gatewayToken":"tok_mock_123","brand":"VISA","last4":"4242"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.last4").value("4242"));

        mockMvc.perform(delete("/api/users/me/addresses/{id}", addressId).with(asCustomer(userId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void rbac_customerCannotListAllCustomers_adminCan() throws Exception {
        mockMvc.perform(get("/api/users").with(asCustomer(UUID.randomUUID())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users").with(asAdmin()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/staff").with(asAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }
}
