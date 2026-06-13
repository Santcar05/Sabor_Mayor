package com.sabormayor.user.web.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String label,
        String street,
        String city,
        String notes,
        boolean defaultAddress) {
}
