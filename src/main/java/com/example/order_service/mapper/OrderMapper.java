package com.example.order_service.mapper;

import com.example.order_service.dto.OrderItemResponse;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {
    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(OrderStatus.valueOf(order.getOrderStatus().name()));
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(items);
        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse res = new OrderItemResponse();
        res.setProductId(item.getProductId());
        res.setQuantity(item.getQuantity());
        res.setPrice(item.getPrice());
        return res;
    }
}
