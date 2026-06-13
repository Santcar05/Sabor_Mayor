package com.sabormayor.menu.web.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record DishResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        boolean available,
        boolean featured,
        String imageUrl,
        Integer prepMinutes,
        String categorySlug,
        String categoryName,
        Set<String> tags,
        Set<String> allergens,
        Set<UUID> pairingIds) {
}
