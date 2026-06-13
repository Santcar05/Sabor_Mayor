package com.sabormayor.order.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.order.domain.Cart;
import com.sabormayor.order.domain.CartItem;
import com.sabormayor.order.infrastructure.CartRepository;
import com.sabormayor.order.web.dto.CartItemRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public Cart getOrCreate(UUID customerId) {
        return cartRepository.findById(customerId)
                .orElseGet(() -> cartRepository.save(Cart.builder().customerId(customerId).build()));
    }

    @Transactional
    public Cart addItem(UUID customerId, CartItemRequest request) {
        Cart cart = getOrCreate(customerId);
        cart.getItems().stream()
                .filter(i -> i.getDishId().equals(request.dishId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.quantity()),
                        () -> cart.getItems().add(CartItem.builder()
                                .cart(cart)
                                .dishId(request.dishId())
                                .quantity(request.quantity())
                                .notes(request.notes())
                                .build()));
        return cart;
    }

    @Transactional
    public Cart removeItem(UUID customerId, UUID itemId) {
        Cart cart = getOrCreate(customerId);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw ResourceNotFoundException.of("Cart item", itemId);
        }
        return cart;
    }

    @Transactional
    public void clear(UUID customerId) {
        cartRepository.findById(customerId).ifPresent(cart -> cart.getItems().clear());
    }
}
