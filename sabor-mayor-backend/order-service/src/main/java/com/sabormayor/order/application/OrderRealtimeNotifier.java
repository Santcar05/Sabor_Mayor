package com.sabormayor.order.application;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.sabormayor.order.domain.Order;
import com.sabormayor.order.mapper.OrderMapper;

import lombok.RequiredArgsConstructor;

/**
 * Pushes order changes over STOMP:
 *  - /topic/kitchen           -> Kitchen Display System (all stations)
 *  - /topic/waiter            -> waiter app (table/board overview)
 *  - /topic/orders/{orderId}  -> the customer tracking their order
 */
@Component
@RequiredArgsConstructor
public class OrderRealtimeNotifier {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderMapper orderMapper;

    public void orderChanged(Order order) {
        var payload = orderMapper.toResponse(order);
        messagingTemplate.convertAndSend("/topic/kitchen", payload);
        messagingTemplate.convertAndSend("/topic/waiter", payload);
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), payload);
    }
}
