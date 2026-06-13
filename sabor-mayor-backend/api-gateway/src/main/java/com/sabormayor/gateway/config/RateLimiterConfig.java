package com.sabormayor.gateway.config;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /** Rate-limit key: authenticated principal when present, client IP otherwise. */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(java.security.Principal::getName)
                .switchIfEmpty(Mono.just(Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(InetSocketAddress::getAddress)
                        .map(java.net.InetAddress::getHostAddress)
                        .orElse("unknown")));
    }
}
