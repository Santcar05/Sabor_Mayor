package com.sabormayor.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Sabor Mayor — Inventory Service")
                .description("Ingredients, stock, low-stock alerts and estimated consumption from orders")
                .version("1.0.0"));
    }
}
