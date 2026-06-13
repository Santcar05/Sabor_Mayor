package com.sabormayor.menu.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Estimated production cost, used for margin reports (admin only). */
    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "prep_minutes")
    private Integer prepMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_tags", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_allergens", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "allergen", length = 50)
    @Builder.Default
    private Set<String> allergens = new LinkedHashSet<>();

    /** Suggested pairings (other dishes/wines of the menu). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_pairings", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "paired_dish_id")
    @Builder.Default
    private Set<UUID> pairingIds = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public BigDecimal marginPercent() {
        if (cost == null || price == null || price.signum() == 0) {
            return null;
        }
        return price.subtract(cost)
                .multiply(BigDecimal.valueOf(100))
                .divide(price, 2, RoundingMode.HALF_UP);
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
