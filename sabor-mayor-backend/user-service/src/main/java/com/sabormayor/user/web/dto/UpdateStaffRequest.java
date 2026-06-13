package com.sabormayor.user.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateStaffRequest(
        @Size(max = 100) String position,
        boolean active) {
}
