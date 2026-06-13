package com.sabormayor.analytics.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabormayor.analytics.domain.DishSalesFact;

public interface DishSalesFactRepository extends JpaRepository<DishSalesFact, UUID> {

    Optional<DishSalesFact> findByDishIdAndSaleDate(UUID dishId, LocalDate saleDate);

    @Query("""
            select d.dishName as dishName, sum(d.quantitySold) as quantity, sum(d.revenue) as revenue
            from DishSalesFact d
            where d.saleDate between :from and :to
            group by d.dishName
            order by sum(d.quantitySold) desc
            """)
    List<TopDish> topDishes(@Param("from") LocalDate from, @Param("to") LocalDate to);

    interface TopDish {
        String getDishName();

        long getQuantity();

        BigDecimal getRevenue();
    }
}
