package com.sabormayor.user.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tokenized reference returned by the payment gateway. We NEVER store card
 * numbers, CVV or expiry beyond what the gateway exposes for display.
 */
@Entity
@Table(name = "payment_method_refs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRef {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "gateway_token", nullable = false, unique = true)
    private String gatewayToken;

    @Column(nullable = false, length = 30)
    private String brand;

    @Column(name = "last4", nullable = false, length = 4)
    private String last4;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
