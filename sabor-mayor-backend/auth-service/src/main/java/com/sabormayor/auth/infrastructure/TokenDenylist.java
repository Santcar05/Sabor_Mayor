package com.sabormayor.auth.infrastructure;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Redis-backed denylist of revoked access-token JTIs. Entries expire together
 * with the token so the set stays small.
 */
@Component
@RequiredArgsConstructor
public class TokenDenylist {

    private static final Logger log = LoggerFactory.getLogger(TokenDenylist.class);
    private static final String PREFIX = "auth:revoked-jti:";

    private final StringRedisTemplate redisTemplate;

    public void revoke(String jti, Duration ttl) {
        if (jti == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(PREFIX + jti, "1", ttl);
        } catch (Exception ex) {
            log.warn("Could not write revoked JTI to Redis: {}", ex.getMessage());
        }
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
        } catch (Exception ex) {
            log.warn("Could not check revoked JTI in Redis: {}", ex.getMessage());
            return false;
        }
    }
}
