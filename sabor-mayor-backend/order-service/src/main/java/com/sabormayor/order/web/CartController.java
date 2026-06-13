package com.sabormayor.order.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.order.application.CartService;
import com.sabormayor.order.mapper.OrderMapper;
import com.sabormayor.order.web.dto.CartItemRequest;
import com.sabormayor.order.web.dto.OrderDtos;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;
    private final OrderMapper mapper;

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping
    public OrderDtos.CartResponse myCart(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponse(cartService.getOrCreate(userId(jwt)));
    }

    @PostMapping("/items")
    public OrderDtos.CartResponse addItem(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequest request) {
        return mapper.toResponse(cartService.addItem(userId(jwt), request));
    }

    @DeleteMapping("/items/{itemId}")
    public OrderDtos.CartResponse removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID itemId) {
        return mapper.toResponse(cartService.removeItem(userId(jwt), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(userId(jwt));
        return ResponseEntity.noContent().build();
    }
}
