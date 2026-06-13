package com.sabormayor.order.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.order.domain.KitchenStation;
import com.sabormayor.order.domain.Order;
import com.sabormayor.order.domain.OrderItem;
import com.sabormayor.order.domain.OrderItemStatus;
import com.sabormayor.order.domain.OrderStatus;
import com.sabormayor.order.infrastructure.OrderItemRepository;
import com.sabormayor.order.infrastructure.OrderRepository;

import lombok.RequiredArgsConstructor;

/** Kitchen Display System: per-station queues and item-level progress. */
@Service
@RequiredArgsConstructor
public class KitchenService {

    private static final List<OrderStatus> ACTIVE = List.of(
            OrderStatus.CONFIRMED, OrderStatus.IN_KITCHEN, OrderStatus.READY);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final OrderRealtimeNotifier realtimeNotifier;

    @Transactional(readOnly = true)
    public List<Order> getActiveOrders(KitchenStation station) {
        List<Order> orders = orderRepository.findByStatusInOrderByCreatedAtAsc(ACTIVE);
        if (station == null) {
            return orders;
        }
        return orders.stream()
                .filter(o -> o.getItems().stream().anyMatch(i -> i.getStation() == station))
                .toList();
    }

    @Transactional
    public Order updateItemStatus(UUID itemId, OrderItemStatus status) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order item", itemId));
        item.setItemStatus(status);
        Order order = item.getOrder();

        // Order-level status follows the items
        if (status == OrderItemStatus.IN_PROGRESS && order.getStatus() == OrderStatus.CONFIRMED) {
            return orderService.updateStatus(order.getId(), OrderStatus.IN_KITCHEN);
        }
        boolean allReady = order.getItems().stream()
                .allMatch(i -> i.getItemStatus() == OrderItemStatus.READY
                        || i.getItemStatus() == OrderItemStatus.SERVED);
        if (status == OrderItemStatus.READY && allReady && order.getStatus() == OrderStatus.IN_KITCHEN) {
            return orderService.updateStatus(order.getId(), OrderStatus.READY);
        }
        realtimeNotifier.orderChanged(order);
        return order;
    }
}
