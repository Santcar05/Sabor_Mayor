package com.sabormayor.order.domain;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_KITCHEN,
    READY,
    SERVED,
    DELIVERED,
    PAID,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            CREATED, Set.of(CONFIRMED, CANCELLED),
            CONFIRMED, Set.of(IN_KITCHEN, PAID, CANCELLED),
            IN_KITCHEN, Set.of(READY, PAID),
            READY, Set.of(SERVED, DELIVERED, PAID),
            SERVED, Set.of(PAID),
            DELIVERED, Set.of(PAID),
            PAID, Set.of(),
            CANCELLED, Set.of());

    public boolean canTransitionTo(OrderStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
