package com.sabormayor.analytics.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabormayor.analytics.domain.SalesFact;

public interface SalesFactRepository extends JpaRepository<SalesFact, UUID> {

    @Query("""
            select coalesce(sum(s.total), 0) from SalesFact s
            where s.saleDate between :from and :to
            """)
    BigDecimal totalRevenue(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(s) from SalesFact s where s.saleDate between :from and :to")
    long orderCount(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select s.saleDate as day, sum(s.total) as revenue, count(s) as orders
            from SalesFact s
            where s.saleDate between :from and :to
            group by s.saleDate
            order by s.saleDate
            """)
    List<DailyRevenue> dailyRevenue(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select count(distinct s.customerId) from SalesFact s
            where s.saleDate between :from and :to
            """)
    long distinctCustomers(@Param("from") LocalDate from, @Param("to") LocalDate to);

    interface DailyRevenue {
        LocalDate getDay();

        BigDecimal getRevenue();

        long getOrders();
    }
}
