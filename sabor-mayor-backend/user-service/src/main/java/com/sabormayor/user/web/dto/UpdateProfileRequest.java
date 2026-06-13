package com.sabormayor.user.web.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 255) String fullName,
        @Size(max = 30) String phone,
        Set<String> dietaryPreferences,
        Set<String> allergies) {
}
