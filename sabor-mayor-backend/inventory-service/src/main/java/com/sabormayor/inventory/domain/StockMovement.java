package com.sabormayor.inventory.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_movements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    public enum Type {
        INBOUND, CONSUMPTION, ADJUSTMENT
    }

    @Id
    private UUID id;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Type type;

    /** Signed quantity: positive for inbound, negative for consumption. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }
}
