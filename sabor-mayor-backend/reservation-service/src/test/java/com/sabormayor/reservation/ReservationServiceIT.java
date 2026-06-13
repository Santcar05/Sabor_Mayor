package com.sabormayor.reservation;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.hamcrest.Matchers;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "app.reminders.enabled=false",
        "management.tracing.enabled=false"
})
class ReservationServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    static final LocalDate NEXT_FRIDAY = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));

    private RequestPostProcessor asCustomer(UUID id) {
        return jwt().jwt(j -> j.subject(id.toString())
                        .claim("email", "res@sabormayor.com").claim("role", "CLIENTE"))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private RequestPostProcessor asAdmin() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("role", "ADMIN"))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void availabilityIsPublicAndReflectsBookings() throws Exception {
        UUID customer = UUID.randomUUID();

        mockMvc.perform(get("/api/reservations/availability?date={d}&partySize=4", NEXT_FRIDAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].remainingSeats").value(50));

        mockMvc.perform(post("/api/reservations").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"12:00","partySize":4}
                                """.formatted(NEXT_FRIDAY)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.depositRequired").value(false));

        mockMvc.perform(get("/api/reservations/availability?date={d}&partySize=4", NEXT_FRIDAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].remainingSeats").value(46));
    }

    @Test
    void groupsOf8RequireDeposit_andCancellationRestoresCapacity() throws Exception {
        UUID customer = UUID.randomUUID();
        LocalDate saturday = NEXT_FRIDAY.plusDays(1);

        String body = mockMvc.perform(post("/api/reservations").with(asCustomer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"20:00","partySize":10,
                                 "preOrderItems":[{"dishId":"20000000-0000-0000-0000-000000000001","quantity":10}]}
                                """.formatted(saturday)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.depositRequired").value(true))
                .andExpect(jsonPath("$.depositAmount").value(300000.0))
                .andExpect(jsonPath("$.preOrderItems", Matchers.hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        String reservationId = JsonPath.read(body, "$.id");

        mockMvc.perform(delete("/api/reservations/{id}", reservationId).with(asCustomer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/reservations/availability?date={d}&partySize=10", saturday))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.time == '20:00:00')].remainingSeats").value(50));
    }

    @Test
    void blockedDatesRejectReservations() throws Exception {
        LocalDate target = NEXT_FRIDAY.plusWeeks(1);

        mockMvc.perform(post("/api/reservations/blocked-dates?date={d}&reason=Evento privado", target)
                        .with(asAdmin()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reservations/availability?date={d}&partySize=2", target))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));

        mockMvc.perform(post("/api/reservations").with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"13:00","partySize":2}
                                """.formatted(target)))
                .andExpect(status().isUnprocessableEntity());

        // Only admins can block dates
        mockMvc.perform(post("/api/reservations/blocked-dates?date={d}", target.plusDays(1))
                        .with(asCustomer(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void capacityIsEnforcedPerSlot() throws Exception {
        LocalDate sunday = NEXT_FRIDAY.plusDays(2);

        // Sunday capacity is 40; a 35-seat booking leaves only 5
        mockMvc.perform(post("/api/reservations").with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"13:00","partySize":30}
                                """.formatted(sunday)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reservations").with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"13:00","partySize":15}
                                """.formatted(sunday)))
                .andExpect(status().isUnprocessableEntity());

        // Outside opening hours
        mockMvc.perform(post("/api/reservations").with(asCustomer(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"%s","time":"23:00","partySize":2}
                                """.formatted(sunday)))
                .andExpect(status().isUnprocessableEntity());
    }
}
