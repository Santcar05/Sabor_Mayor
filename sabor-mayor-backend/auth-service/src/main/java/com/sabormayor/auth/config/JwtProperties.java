package com.sabormayor.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * If privateKeyPem/publicKeyPem are blank an ephemeral RSA key pair is
 * generated at startup (dev only: restarting invalidates issued tokens).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        Duration accessTtl,
        Duration refreshTtl,
        String privateKeyPem,
        String publicKeyPem) {
}
