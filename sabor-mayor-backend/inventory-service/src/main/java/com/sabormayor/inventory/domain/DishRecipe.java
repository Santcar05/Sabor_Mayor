package com.sabormayor.inventory.domain;

import java.math.BigDecimal;
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

/** How much of an ingredient one unit of a dish consumes (for estimated depletion). */
@Entity
@Table(name = "dish_recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishRecipe {

    @Id
    private UUID id;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "quantity_per_dish", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityPerDish;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
