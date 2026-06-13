package com.sabormayor.order.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.order.domain.Order;
import com.sabormayor.order.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    List<Order> findByTableIdAndStatusIn(UUID tableId, List<OrderStatus> statuses);
}
