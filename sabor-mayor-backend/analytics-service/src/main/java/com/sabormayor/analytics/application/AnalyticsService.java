package com.sabormayor.analytics.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.analytics.domain.DishSalesFact;
import com.sabormayor.analytics.domain.ReservationFact;
import com.sabormayor.analytics.domain.SalesFact;
import com.sabormayor.analytics.infrastructure.DishSalesFactRepository;
import com.sabormayor.analytics.infrastructure.ReservationFactRepository;
import com.sabormayor.analytics.infrastructure.SalesFactRepository;
import com.sabormayor.analytics.web.dto.DashboardResponse;
import com.sabormayor.common.events.OrderLine;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SalesFactRepository salesRepository;
    private final DishSalesFactRepository dishSalesRepository;
    private final ReservationFactRepository reservationRepository;

    /** Idempotent: keyed by orderId. */
    @Transactional
    public void recordSale(OrderPaidEvent event) {
        if (salesRepository.existsById(event.orderId())) {
            return;
        }
        LocalDate day = event.occurredAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        int itemCount = event.items().stream().mapToInt(OrderLine::quantity).sum();
        salesRepository.save(SalesFact.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .saleDate(day)
                .total(event.total())
                .tip(event.tip() != null ? event.tip() : BigDecimal.ZERO)
                .itemCount(itemCount)
                .build());

        for (OrderLine line : event.items()) {
            DishSalesFact fact = dishSalesRepository.findByDishIdAndSaleDate(line.dishId(), day)
                    .orElseGet(() -> DishSalesFact.builder()
                            .dishId(line.dishId())
                            .dishName(line.dishName())
                            .saleDate(day)
                            .quantitySold(0)
                            .revenue(BigDecimal.ZERO)
                            .build());
            fact.setQuantitySold(fact.getQuantitySold() + line.quantity());
            fact.setRevenue(fact.getRevenue().add(
                    line.unitPrice().multiply(BigDecimal.valueOf(line.quantity()))));
            dishSalesRepository.save(fact);
        }
    }

    @Transactional
    public void recordReservation(ReservationCreatedEvent event) {
        if (reservationRepository.existsById(event.reservationId())) {
            return;
        }
        reservationRepository.save(ReservationFact.builder()
                .reservationId(event.reservationId())
                .reservationDate(event.date())
                .partySize(event.partySize())
                .cancelled(false)
                .build());
    }

    @Transactional
    public void markReservationCancelled(java.util.UUID reservationId) {
        reservationRepository.findById(reservationId).ifPresent(fact -> fact.setCancelled(true));
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(LocalDate from, LocalDate to) {
        BigDecimal revenue = salesRepository.totalRevenue(from, to);
        long orders = salesRepository.orderCount(from, to);
        long customers = salesRepository.distinctCustomers(from, to);
        long reservations = reservationRepository.total(from, to);
        long cancelled = reservationRepository.cancelled(from, to);
        double noShowRate = reservations == 0 ? 0.0 : (double) cancelled / reservations;
        BigDecimal avgTicket = orders == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orders), 2, java.math.RoundingMode.HALF_UP);

        List<DashboardResponse.TopDish> top = dishSalesRepository.topDishes(from, to).stream()
                .limit(10)
                .map(d -> new DashboardResponse.TopDish(d.getDishName(), d.getQuantity(), d.getRevenue()))
                .toList();
        List<DashboardResponse.DailyPoint> daily = salesRepository.dailyRevenue(from, to).stream()
                .map(d -> new DashboardResponse.DailyPoint(d.getDay(), d.getRevenue(), d.getOrders()))
                .toList();

        return new DashboardResponse(from, to, revenue, orders, customers, avgTicket,
                reservations, cancelled, noShowRate, top, daily);
    }

    @Transactional(readOnly = true)
    public List<DishSalesFactRepository.TopDish> topDishes(LocalDate from, LocalDate to) {
        return dishSalesRepository.topDishes(from, to);
    }
}
