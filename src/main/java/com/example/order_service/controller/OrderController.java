package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request,
                                                     @RequestHeader("X-User-Id") String userId) {
        Long buyerId = Long.valueOf(userId);
        Order order = orderService.createOrder(request, buyerId);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> myOrders(@RequestHeader("X-User-Id") String userId) {
        Long buyerId = Long.valueOf(userId);
        List<Order> orders = orderService.getOrdersByBuyer(buyerId);
        return ResponseEntity.ok(orders.stream()
                .map(orderMapper::toResponse)
                .toList());
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> sellerOrders(@RequestHeader("X-User-Id") String userId) {
        Long sellerId = Long.valueOf(userId);
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        return ResponseEntity.ok(orders.stream()
                .map(orderMapper::toResponse)
                .toList());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId,
                                            @RequestHeader("X-User-Id") String userId) {
        Long currentUserId = Long.valueOf(userId);
        orderService.deleteOrder(orderId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}