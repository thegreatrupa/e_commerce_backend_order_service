package com.example.order_service.dto;

import com.example.order_service.entity.OrderItem;

import java.util.List;

public class CreateOrderRequest {
    private List<OrderItemRequest> items;

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
