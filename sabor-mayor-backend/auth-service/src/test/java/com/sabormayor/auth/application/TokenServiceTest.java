package com.sabormayor.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.sabormayor.auth.config.JwtProperties;
import com.sabormayor.auth.domain.AuthProvider;
import com.sabormayor.auth.domain.RefreshToken;
import com.sabormayor.auth.domain.Role;
import com.sabormayor.auth.domain.UserAccount;
import com.sabormayor.auth.infrastructure.RefreshTokenRepository;
import com.sabormayor.common.error.ApiException;

class TokenServiceTest {

    private TokenService tokenService;
    private RefreshTokenRepository refreshTokenRepository;
    private NimbusJwtDecoder decoder;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate())
                .keyID("test-key")
                .build();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) pair.getPublic()).build();

        refreshTokenRepository = mock(RefreshTokenRepository.class);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JwtProperties properties = new JwtProperties(
                "https://test.sabormayor.com", Duration.ofMinutes(15), Duration.ofDays(7), "", "");
        tokenService = new TokenService(encoder, properties, refreshTokenRepository);
    }

    private UserAccount user(Role role) {
        return UserAccount.builder()
                .id(UUID.randomUUID())
                .email("test@sabormayor.com")
                .fullName("Test User")
                .role(role)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
    }

    @Test
    void issuedAccessTokenContainsSubjectRoleAndEmail() {
        UserAccount user = user(Role.MESERO);

        TokenPair pair = tokenService.issue(user);

        Jwt jwt = decoder.decode(pair.accessToken());
        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("MESERO");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("test@sabormayor.com");
        assertThat(jwt.getId()).isNotBlank();
        assertThat(pair.refreshToken()).isNotBlank();
        assertThat(pair.expiresInSeconds()).isEqualTo(Duration.ofMinutes(15).toSeconds());
    }

    @Test
    void refreshTokenIsStoredHashedNotRaw() {
        TokenPair pair = tokenService.issue(user(Role.CLIENTE));

        org.mockito.ArgumentCaptor<RefreshToken> captor = org.mockito.ArgumentCaptor.forClass(RefreshToken.class);
        org.mockito.Mockito.verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash())
                .isNotEqualTo(pair.refreshToken())
                .isEqualTo(TokenService.sha256(pair.refreshToken()));
    }

    @Test
    void validateAndRevokeRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.validateAndRevoke("does-not-exist"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void reusingRevokedTokenRevokesWholeSessionFamily() {
        UUID userId = UUID.randomUUID();
        RefreshToken revoked = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> tokenService.validateAndRevoke("reused-token"))
                .isInstanceOf(ApiException.class);
        org.mockito.Mockito.verify(refreshTokenRepository).revokeAllForUser(userId);
    }
}
