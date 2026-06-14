package com.sabormayor.order.web.dto;

import java.util.List;

import com.sabormayor.order.domain.OrderType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotNull OrderType type,
        String tableId,
        @Size(max = 500) String deliveryAddress,
        @Size(max = 500) String notes,
        Boolean fromCart,
        @Valid List<OrderItemRequest> items) {
}
