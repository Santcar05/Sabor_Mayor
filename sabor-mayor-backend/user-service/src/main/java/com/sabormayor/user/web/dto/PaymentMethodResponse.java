package com.sabormayor.user.web.dto;

import java.util.UUID;

public record PaymentMethodResponse(
        UUID id,
        String brand,
        String last4) {
}
