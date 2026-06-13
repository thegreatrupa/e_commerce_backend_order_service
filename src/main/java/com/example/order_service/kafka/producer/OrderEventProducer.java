package com.example.order_service.kafka.producer;

import com.example.order_service.kafka.event.OrderCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreateEvent event){
        kafkaTemplate.send(
                "order-created",
                event.orderId().toString(),
                event
        ).whenComplete((result, ex) -> {
            if(ex != null){
                log.error("Failed to publish event. orderId={}", event.orderId(), ex);
                return;
            }
            log.info("Publisher order-created event. orderId={}", event.orderId());
        });

    }
}
