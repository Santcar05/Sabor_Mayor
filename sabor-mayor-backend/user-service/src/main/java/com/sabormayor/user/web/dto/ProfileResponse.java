package com.sabormayor.user.web.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        Set<String> dietaryPreferences,
        Set<String> allergies,
        boolean frequentGuest,
        int visits,
        Instant createdAt) {
}
