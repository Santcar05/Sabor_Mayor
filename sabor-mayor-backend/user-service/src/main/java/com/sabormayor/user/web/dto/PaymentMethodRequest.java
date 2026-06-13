package com.sabormayor.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Only gateway token references — never raw card data. */
public record PaymentMethodRequest(
        @NotBlank @Size(max = 255) String gatewayToken,
        @NotBlank @Size(max = 30) String brand,
        @NotBlank @Pattern(regexp = "\\d{4}") String last4) {
}
