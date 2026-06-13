package com.sabormayor.order.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.common.security.Roles;
import com.sabormayor.order.application.OrderService;
import com.sabormayor.order.mapper.OrderMapper;
import com.sabormayor.order.web.dto.OrderDtos;
import com.sabormayor.order.web.dto.OrderItemRequest;
import com.sabormayor.order.web.dto.PlaceOrderRequest;
import com.sabormayor.order.web.dto.SplitResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private static final String STAFF = "hasAnyRole('" + Roles.MESERO + "','" + Roles.COCINERO + "','"
            + Roles.ADMIN + "','" + Roles.SUPER_ADMIN + "')";

    private final OrderService orderService;
    private final OrderMapper mapper;

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private boolean isStaff(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        return role != null && !Roles.CLIENTE.equals(role);
    }

    @PostMapping
    @Operation(summary = "Place an order (dine-in/delivery/pickup), from the cart or with explicit items")
    public ResponseEntity<OrderDtos.OrderResponse> placeOrder(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PlaceOrderRequest request) {
        var order = orderService.placeOrder(userId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(order));
    }

    @GetMapping("/me")
    public List<OrderDtos.OrderResponse> myOrders(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponses(orderService.getMyOrders(userId(jwt)));
    }

    @GetMapping("/{orderId}")
    public OrderDtos.OrderResponse getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return mapper.toResponse(orderService.getOrder(orderId, userId(jwt), isStaff(jwt)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize(STAFF)
    @Operation(summary = "Move the order through its state machine (staff)")
    public OrderDtos.OrderResponse updateStatus(@PathVariable UUID orderId,
            @Valid @RequestBody OrderDtos.UpdateStatus request) {
        return mapper.toResponse(orderService.updateStatus(orderId, request.status()));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Customer cancels while the kitchen has not started")
    public OrderDtos.OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return mapper.toResponse(orderService.cancelByCustomer(orderId, userId(jwt)));
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize(STAFF)
    @Operation(summary = "Waiter adds items to an open order")
    public OrderDtos.OrderResponse addItems(@PathVariable UUID orderId,
            @Valid @RequestBody List<OrderItemRequest> items) {
        return mapper.toResponse(orderService.addItems(orderId, items));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @PreAuthorize(STAFF)
    public OrderDtos.OrderResponse removeItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return mapper.toResponse(orderService.removeItem(orderId, itemId));
    }

    @GetMapping("/{orderId}/split")
    @PreAuthorize(STAFF)
    @Operation(summary = "Equal split of the bill for the waiter app")
    public SplitResponse split(@PathVariable UUID orderId, @RequestParam int parts) {
        return orderService.split(orderId, parts);
    }
}
