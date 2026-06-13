package com.sabormayor.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 60) String label,
        @NotBlank @Size(max = 255) String street,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 500) String notes,
        boolean defaultAddress) {
}
