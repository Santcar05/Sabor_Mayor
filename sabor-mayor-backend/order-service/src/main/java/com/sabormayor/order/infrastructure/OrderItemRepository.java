package com.sabormayor.order.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
