package com.example.order_service.kafka.event;

public record OrderCreateEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        Long userId
) {
}
