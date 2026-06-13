package com.sabormayor.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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

/** Aggregated dish sales per day (materialized read model). */
@Entity
@Table(name = "dish_sales_facts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishSalesFact {

    @Id
    private UUID id;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "dish_name", nullable = false)
    private String dishName;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "quantity_sold", nullable = false)
    private int quantitySold;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal revenue;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
