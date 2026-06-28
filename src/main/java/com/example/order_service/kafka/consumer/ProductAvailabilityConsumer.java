package com.example.order_service.kafka.consumer;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.kafka.event.ProductAvailableEvent;
import com.example.order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductAvailabilityConsumer {

    private final OrderRepository orderRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductAvailabilityConsumer.class);

    public ProductAvailabilityConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = "product-available",
            groupId = "order-service-group"
    )
    public void handleProductAvailable(ProductAvailableEvent event){
        Order order = orderRepository.findById(event.orderId()).orElseThrow();
        if(event.isAvailable()) {
            order.setTotalAmount(order.getTotalAmount() + event.price());
            order.setOrderStatus(OrderStatus.CONFIRMED);
            log.info("order confirmed {}", order.getId());
        }
        else {
            order.setTotalAmount(0.0);
            order.setOrderStatus(OrderStatus.CANCELLED);
            log.info("order cancelled {}", event.orderId());
        }
        orderRepository.save(order);

    }

}
