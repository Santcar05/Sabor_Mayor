package com.sabormayor.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Sabor Mayor — Reservation Service")
                .description("Reservations, availability, blocked dates, deposits, pre-orders and reminders")
                .version("1.0.0"));
    }
}
