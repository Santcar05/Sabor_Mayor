package com.sabormayor.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Sabor Mayor — Auth Service")
                .description("Registration, login, OAuth2, JWT/refresh token management and RBAC")
                .version("1.0.0"));
    }
}
