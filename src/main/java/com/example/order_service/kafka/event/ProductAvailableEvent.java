package com.example.order_service.kafka.event;

public record ProductAvailableEvent(
        Long orderId,
        Long productId,
        boolean isAvailable,
        String reason,
        Double price,
        Long sellerId
) {
}
