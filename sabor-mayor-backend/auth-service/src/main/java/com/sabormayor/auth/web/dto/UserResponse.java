package com.sabormayor.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String role,
        String provider,
        boolean enabled,
        Instant createdAt) {
}
