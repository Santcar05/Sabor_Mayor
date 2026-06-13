package com.sabormayor.order.web;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.order.application.KitchenService;
import com.sabormayor.order.domain.KitchenStation;
import com.sabormayor.order.mapper.OrderMapper;
import com.sabormayor.order.web.dto.OrderDtos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kitchen")
@PreAuthorize("hasAnyRole('COCINERO','ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Kitchen Display System")
public class KitchenController {

    private final KitchenService kitchenService;
    private final OrderMapper mapper;

    @GetMapping("/orders")
    @Operation(summary = "Active orders for the KDS, optionally filtered by station")
    public List<OrderDtos.OrderResponse> activeOrders(@RequestParam(required = false) KitchenStation station) {
        return mapper.toResponses(kitchenService.getActiveOrders(station));
    }

    @PatchMapping("/items/{itemId}/status")
    @Operation(summary = "Progress a single item; the order status follows automatically")
    public OrderDtos.OrderResponse updateItemStatus(@PathVariable UUID itemId,
            @Valid @RequestBody OrderDtos.UpdateItemStatus request) {
        return mapper.toResponse(kitchenService.updateItemStatus(itemId, request.status()));
    }
}
