package com.sabormayor.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sabormayor.common.error.BusinessRuleException;

class OrderStateMachineTest {

    private Order order(OrderStatus status) {
        return Order.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type(OrderType.DINE_IN)
                .status(status)
                .subtotal(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();
    }

    @Test
    void happyPathDineIn() {
        Order order = order(OrderStatus.CREATED);
        order.transitionTo(OrderStatus.CONFIRMED);
        order.transitionTo(OrderStatus.IN_KITCHEN);
        order.transitionTo(OrderStatus.READY);
        order.transitionTo(OrderStatus.SERVED);
        order.transitionTo(OrderStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void cannotSkipKitchen() {
        Order order = order(OrderStatus.CREATED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.READY))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cannotCancelOnceInKitchen() {
        Order order = order(OrderStatus.IN_KITCHEN);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void paidAndCancelledAreTerminal() {
        assertThatThrownBy(() -> order(OrderStatus.PAID).transitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> order(OrderStatus.CANCELLED).transitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void totalsIncludeTip() {
        Order order = order(OrderStatus.CREATED);
        order.getItems().add(OrderItem.builder()
                .order(order).dishId(UUID.randomUUID()).dishName("Test")
                .unitPrice(new BigDecimal("10000")).quantity(3)
                .station(KitchenStation.CALIENTES).itemStatus(OrderItemStatus.PENDING)
                .build());
        order.setTip(new BigDecimal("2000"));
        order.recalculateTotals();
        assertThat(order.getSubtotal()).isEqualByComparingTo("30000");
        assertThat(order.getTotal()).isEqualByComparingTo("32000");
    }
}
