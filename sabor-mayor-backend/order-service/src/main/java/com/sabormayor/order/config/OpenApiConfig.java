package com.sabormayor.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Sabor Mayor — Order Service")
                .description("Cart, orders, tables/QR, Kitchen Display System, waiter app, real-time WebSockets")
                .version("1.0.0"));
    }
}
