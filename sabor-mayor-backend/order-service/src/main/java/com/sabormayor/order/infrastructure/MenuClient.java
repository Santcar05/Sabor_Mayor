package com.sabormayor.order.infrastructure;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Synchronous lookup against menu-service, protected by a Resilience4j
 * circuit breaker (see application.yml). Prices always come from menu-service,
 * never from the client request.
 */
@FeignClient(name = "menu-service")
public interface MenuClient {

    @GetMapping("/api/menu/dishes/by-ids")
    List<MenuDish> getDishesByIds(@RequestParam("ids") List<UUID> ids);

    record MenuDish(
            UUID id,
            String name,
            String slug,
            BigDecimal price,
            boolean available,
            String categorySlug,
            Set<String> tags) {
    }
}
