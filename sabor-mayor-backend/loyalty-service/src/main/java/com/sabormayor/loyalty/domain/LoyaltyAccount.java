package com.sabormayor.loyalty.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    /** Equals the auth-service user id. */
    @Id
    @Column(name = "user_id")
    private UUID userId;

    /** Redeemable balance. */
    @Column(nullable = false)
    private int points;

    /** Total earned ever; determines the level (redeeming does not demote). */
    @Column(name = "lifetime_points", nullable = false)
    private int lifetimePoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoyaltyLevel level;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void earn(int earned) {
        points += earned;
        lifetimePoints += earned;
        level = LoyaltyLevel.fromLifetimePoints(lifetimePoints);
    }

    public void redeem(int redeemed) {
        points -= redeemed;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
