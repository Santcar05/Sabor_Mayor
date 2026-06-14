package com.sabormayor.order.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.BusinessRuleException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.common.events.OrderLine;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.OrderPlacedEvent;
import com.sabormayor.common.events.OrderStatusChangedEvent;
import com.sabormayor.common.events.PaymentConfirmedEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.order.domain.KitchenStation;
import com.sabormayor.order.domain.Order;
import com.sabormayor.order.domain.OrderItem;
import com.sabormayor.order.domain.OrderStatus;
import com.sabormayor.order.domain.OrderType;
import com.sabormayor.order.domain.RestaurantTable;
import com.sabormayor.order.domain.TableStatus;
import com.sabormayor.order.infrastructure.MenuClient;
import com.sabormayor.order.infrastructure.OrderRepository;
import com.sabormayor.order.infrastructure.RestaurantTableRepository;
import com.sabormayor.order.web.dto.OrderItemRequest;
import com.sabormayor.order.web.dto.PlaceOrderRequest;
import com.sabormayor.order.web.dto.SplitResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final CartService cartService;
    private final MenuClient menuClient;
    private final OutboxService outboxService;
    private final OrderRealtimeNotifier realtimeNotifier;

    @Transactional
    public Order placeOrder(UUID customerId, PlaceOrderRequest request) {
        List<OrderItemRequest> itemRequests = resolveItems(customerId, request);
        if (itemRequests.isEmpty()) {
            throw new BusinessRuleException("Order must contain at least one item");
        }

        Order order = Order.builder()
                .customerId(customerId)
                .type(request.type())
                .status(OrderStatus.CREATED)
                .deliveryAddress(request.deliveryAddress())
                .notes(request.notes())
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        if (request.type() == OrderType.DINE_IN) {
            if (request.tableId() == null || request.tableId().isBlank()) {
                throw new BusinessRuleException("Dine-in orders require a table");
            }
            RestaurantTable table = tableRepository.findByQrToken(request.tableId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Table", request.tableId()));
            table.setStatus(TableStatus.OCUPADA);
            order.setTableId(table.getId());
        } else if (request.type() == OrderType.DELIVERY && (request.deliveryAddress() == null
                || request.deliveryAddress().isBlank())) {
            throw new BusinessRuleException("Delivery orders require a delivery address");
        }

        addItemsFromMenu(order, itemRequests);
        order.recalculateTotals();
        Order saved = orderRepository.save(order);

        if (Boolean.TRUE.equals(request.fromCart())) {
            cartService.clear(customerId);
        }

        outboxService.append(KafkaTopics.ORDERS_EVENTS, saved.getId(), new OrderPlacedEvent(
                saved.getId(), customerId, saved.getType().name(), saved.getTotal(),
                toOrderLines(saved), Instant.now()));
        realtimeNotifier.orderChanged(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId, UUID requesterId, boolean isStaff) {
        Order order = requireOrder(orderId);
        if (!isStaff && !order.getCustomerId().equals(requesterId)) {
            throw ResourceNotFoundException.of("Order", orderId);
        }
        return order;
    }

    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus target) {
        Order order = requireOrder(orderId);
        OrderStatus previous = order.getStatus();
        order.transitionTo(target);
        if (target == OrderStatus.CANCELLED || target == OrderStatus.PAID) {
            freeTable(order);
        }
        outboxService.append(KafkaTopics.ORDERS_EVENTS, order.getId(), new OrderStatusChangedEvent(
                order.getId(), order.getCustomerId(), previous.name(), target.name(), Instant.now()));
        realtimeNotifier.orderChanged(order);
        return order;
    }

    @Transactional
    public Order cancelByCustomer(UUID orderId, UUID customerId) {
        Order order = requireOrder(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw ResourceNotFoundException.of("Order", orderId);
        }
        if (!order.isModifiable()) {
            throw new BusinessRuleException("Order can no longer be cancelled (status " + order.getStatus() + ")");
        }
        return updateStatus(orderId, OrderStatus.CANCELLED);
    }

    /** Waiter/customer modification while the kitchen has not started. */
    @Transactional
    public Order addItems(UUID orderId, List<OrderItemRequest> items) {
        Order order = requireOrder(orderId);
        if (!order.isModifiable()) {
            throw new BusinessRuleException("Order items can no longer be modified");
        }
        addItemsFromMenu(order, items);
        order.recalculateTotals();
        realtimeNotifier.orderChanged(order);
        return order;
    }

    @Transactional
    public Order removeItem(UUID orderId, UUID itemId) {
        Order order = requireOrder(orderId);
        if (!order.isModifiable()) {
            throw new BusinessRuleException("Order items can no longer be modified");
        }
        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw ResourceNotFoundException.of("Order item", itemId);
        }
        order.recalculateTotals();
        realtimeNotifier.orderChanged(order);
        return order;
    }

    /** Equal split of the bill; the remainder cents go to the first part. */
    @Transactional(readOnly = true)
    public SplitResponse split(UUID orderId, int parts) {
        Order order = requireOrder(orderId);
        if (parts < 2 || parts > 20) {
            throw new BusinessRuleException("Split parts must be between 2 and 20");
        }
        BigDecimal perPart = order.getTotal().divide(BigDecimal.valueOf(parts), 2, RoundingMode.FLOOR);
        BigDecimal remainder = order.getTotal().subtract(perPart.multiply(BigDecimal.valueOf(parts)));
        return new SplitResponse(order.getId(), parts, perPart, perPart.add(remainder), order.getTotal());
    }

    /** Triggered by PaymentConfirmed events from payment-service. */
    @Transactional
    public void markPaid(PaymentConfirmedEvent event) {
        Order order = requireOrder(event.orderId());
        if (order.getStatus() == OrderStatus.PAID) {
            return; // idempotent: event redelivery is harmless
        }
        OrderStatus previous = order.getStatus();
        order.setPaymentId(event.paymentId());
        if (event.tip() != null) {
            order.setTip(event.tip());
            order.recalculateTotals();
        }
        order.transitionTo(OrderStatus.PAID);
        freeTable(order);
        outboxService.append(KafkaTopics.ORDERS_EVENTS, order.getId(), new OrderStatusChangedEvent(
                order.getId(), order.getCustomerId(), previous.name(), OrderStatus.PAID.name(), Instant.now()));
        outboxService.append(KafkaTopics.ORDERS_EVENTS, order.getId(), new OrderPaidEvent(
                order.getId(), order.getCustomerId(), event.paymentId(), order.getTotal(),
                order.getTip(), toOrderLines(order), Instant.now()));
        realtimeNotifier.orderChanged(order);
    }

    Order requireOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    private List<OrderItemRequest> resolveItems(UUID customerId, PlaceOrderRequest request) {
        if (Boolean.TRUE.equals(request.fromCart())) {
            return cartService.getOrCreate(customerId).getItems().stream()
                    .map(i -> new OrderItemRequest(i.getDishId(), i.getQuantity(), i.getNotes()))
                    .toList();
        }
        return request.items() != null ? request.items() : List.of();
    }

    private void addItemsFromMenu(Order order, List<OrderItemRequest> itemRequests) {
        List<UUID> dishIds = itemRequests.stream().map(OrderItemRequest::dishId).distinct().toList();
        Map<UUID, MenuClient.MenuDish> dishes;
        try {
            dishes = menuClient.getDishesByIds(dishIds).stream()
                    .collect(Collectors.toMap(MenuClient.MenuDish::id, Function.identity()));
        } catch (Exception ex) {
            throw new BusinessRuleException("Menu service unavailable, please retry");
        }
        for (OrderItemRequest itemRequest : itemRequests) {
            MenuClient.MenuDish dish = dishes.get(itemRequest.dishId());
            if (dish == null) {
                throw new BusinessRuleException("Dish %s does not exist".formatted(itemRequest.dishId()));
            }
            if (!dish.available()) {
                throw new BusinessRuleException("Dish '%s' is not available".formatted(dish.name()));
            }
            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .dishId(dish.id())
                    .dishName(dish.name())
                    .unitPrice(dish.price())
                    .quantity(itemRequest.quantity())
                    .notes(itemRequest.notes())
                    .station(stationFor(dish.categorySlug()))
                    .build());
        }
    }

    private void freeTable(Order order) {
        if (order.getTableId() != null) {
            tableRepository.findById(order.getTableId())
                    .ifPresent(table -> table.setStatus(TableStatus.LIMPIEZA));
        }
    }

    private List<OrderLine> toOrderLines(Order order) {
        return order.getItems().stream()
                .map(i -> new OrderLine(i.getDishId(), i.getDishName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
    }

    static KitchenStation stationFor(String categorySlug) {
        if (categorySlug == null) {
            return KitchenStation.CALIENTES;
        }
        return switch (categorySlug) {
            case "entradas" -> KitchenStation.FRIOS;
            case "postres" -> KitchenStation.POSTRES;
            case "cocteles", "vinos" -> KitchenStation.BAR;
            default -> KitchenStation.CALIENTES;
        };
    }
}
