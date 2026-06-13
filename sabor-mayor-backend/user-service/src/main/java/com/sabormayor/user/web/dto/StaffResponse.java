package com.sabormayor.user.web.dto;

import java.time.Instant;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        String email,
        String fullName,
        String role,
        String position,
        boolean active,
        Instant hiredAt) {
}
