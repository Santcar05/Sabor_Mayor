package com.sabormayor.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Sabor Mayor — Payment Service")
                .description("Charges, tips and refunds behind a PaymentGateway abstraction")
                .version("1.0.0"));
    }
}
