package com.sabormayor.order.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sabormayor.order.domain.KitchenStation;
import com.sabormayor.order.domain.OrderItemStatus;
import com.sabormayor.order.domain.OrderStatus;
import com.sabormayor.order.domain.OrderType;
import com.sabormayor.order.domain.TableStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record UpdateStatus(@NotNull OrderStatus status) {
    }

    public record UpdateItemStatus(@NotNull OrderItemStatus status) {
    }

    public record UpdateTableStatus(@NotNull TableStatus status) {
    }

    public record CreateTable(@Min(1) int number, @Min(1) @Max(30) int capacity) {
    }

    public record OrderItemResponse(
            UUID id,
            UUID dishId,
            String dishName,
            BigDecimal unitPrice,
            int quantity,
            String notes,
            KitchenStation station,
            OrderItemStatus itemStatus) {
    }

    public record OrderResponse(
            UUID id,
            UUID customerId,
            OrderType type,
            OrderStatus status,
            UUID tableId,
            String deliveryAddress,
            List<OrderItemResponse> items,
            BigDecimal subtotal,
            BigDecimal tip,
            BigDecimal total,
            UUID paymentId,
            String notes,
            Instant createdAt) {
    }

    public record TableResponse(
            UUID id,
            int number,
            int capacity,
            TableStatus status,
            String qrToken) {
    }

    public record CartItemResponse(UUID id, UUID dishId, int quantity, String notes) {
    }

    public record CartResponse(UUID customerId, List<CartItemResponse> items) {
    }
}
