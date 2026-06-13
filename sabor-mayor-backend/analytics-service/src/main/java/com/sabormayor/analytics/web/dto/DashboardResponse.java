package com.sabormayor.analytics.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalRevenue,
        long totalOrders,
        long distinctCustomers,
        BigDecimal averageTicket,
        long totalReservations,
        long cancelledReservations,
        double noShowRate,
        List<TopDish> topDishes,
        List<DailyPoint> dailyRevenue) {

    public record TopDish(String dishName, long quantity, BigDecimal revenue) {
    }

    public record DailyPoint(LocalDate day, BigDecimal revenue, long orders) {
    }
}
