package com.sabormayor.order.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.order.domain.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}
