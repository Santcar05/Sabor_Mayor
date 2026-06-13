package com.sabormayor.order.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.sabormayor.order.domain.Cart;
import com.sabormayor.order.domain.Order;
import com.sabormayor.order.domain.RestaurantTable;
import com.sabormayor.order.web.dto.OrderDtos;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDtos.OrderResponse toResponse(Order order);

    List<OrderDtos.OrderResponse> toResponses(List<Order> orders);

    OrderDtos.TableResponse toResponse(RestaurantTable table);

    List<OrderDtos.TableResponse> toTableResponses(List<RestaurantTable> tables);

    OrderDtos.CartResponse toResponse(Cart cart);
}
