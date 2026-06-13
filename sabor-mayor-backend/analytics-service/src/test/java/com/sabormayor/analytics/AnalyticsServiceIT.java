package com.sabormayor.analytics;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

import com.sabormayor.analytics.application.AnalyticsService;
import com.sabormayor.common.events.OrderLine;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;

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
class AnalyticsServiceIT {

    static final UUID CEVICHE = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AnalyticsService analyticsService;

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void salesAndReservationEventsFeedTheDashboardAndExcelExport() throws Exception {
        LocalDate today = LocalDate.now();
        Instant now = today.atTime(13, 0).toInstant(java.time.ZoneOffset.UTC);

        analyticsService.recordSale(new OrderPaidEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("173000"), new BigDecimal("17300"),
                List.of(new OrderLine(CEVICHE, "Ceviche de Maracuya", 2, new BigDecimal("42000"))), now));
        analyticsService.recordSale(new OrderPaidEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("89000"), BigDecimal.ZERO,
                List.of(new OrderLine(CEVICHE, "Ceviche de Maracuya", 1, new BigDecimal("42000"))), now));
        analyticsService.recordReservation(new ReservationCreatedEvent(UUID.randomUUID(), UUID.randomUUID(),
                "c@x.com", "Cliente", today, LocalTime.of(20, 0), 4, Instant.now()));

        mockMvc.perform(get("/api/analytics/dashboard?from={f}&to={t}", today, today).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(262000.0))
                .andExpect(jsonPath("$.totalOrders").value(2))
                .andExpect(jsonPath("$.averageTicket").value(131000.0))
                .andExpect(jsonPath("$.totalReservations").value(1))
                .andExpect(jsonPath("$.topDishes[0].dishName").value("Ceviche de Maracuya"))
                .andExpect(jsonPath("$.topDishes[0].quantity").value(3));

        mockMvc.perform(get("/api/analytics/dashboard/export.xlsx?from={f}&to={t}", today, today).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString(".xlsx")));
    }

    @Test
    void analyticsIsAdminOnly() throws Exception {
        RequestPostProcessor asCustomer = jwt()
                .jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        mockMvc.perform(get("/api/analytics/dashboard?from={f}&to={t}", LocalDate.now(), LocalDate.now())
                        .with(asCustomer))
                .andExpect(status().isForbidden());
    }
}
