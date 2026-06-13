package com.sabormayor.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(@NotBlank String idToken) {
}
