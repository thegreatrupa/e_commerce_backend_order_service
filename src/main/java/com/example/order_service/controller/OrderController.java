package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.OrderService;
import com.example.order_service.util.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final JwtUtils jwtUtils;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, JwtUtils jwtUtils, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.jwtUtils = jwtUtils;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request,
                                                     @AuthenticationPrincipal Long userId) {
        Order order = orderService.createOrder(request, userId);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> myOrders(@RequestHeader("Authorization") String authHeader, @AuthenticationPrincipal Long userId) {
        List<Order> orders = orderService.getOrdersByBuyer(userId);
        return ResponseEntity.ok(orders.stream()
                .map(orderMapper::toResponse)
                .toList());
    }

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> sellerOrders(@RequestHeader("Authorization") String authHeader, @AuthenticationPrincipal Long userId) {
        List<Order> orders = orderService.getOrdersBySeller(userId);
        return ResponseEntity.ok(orders.stream()
                .map(orderMapper::toResponse)
                .toList());
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Long userId
    ) {
        orderService.deleteOrder(orderId, userId);
    }

}
