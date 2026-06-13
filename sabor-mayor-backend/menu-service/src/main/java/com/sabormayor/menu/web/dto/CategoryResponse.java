package com.sabormayor.menu.web.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        int displayOrder) {
}
