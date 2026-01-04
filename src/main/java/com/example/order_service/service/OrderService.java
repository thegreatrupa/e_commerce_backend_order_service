package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.OrderItemRequest;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.exceptions.ForbiddenException;
import com.example.order_service.exceptions.BadRequestException;
import com.example.order_service.exceptions.ResourceNotFoundException;
import com.example.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String ProductServiceUrl;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, Long buyerId){
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        for(OrderItemRequest it: request.getItems()){
            String productUrl = ProductServiceUrl + "/products/" + it.getProductId();
            ResponseEntity<Map> resp = restTemplate.getForEntity(productUrl, Map.class);
            if(!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null){
                throw new RuntimeException("Product not found: " + it.getProductId());
            }
            Map body = resp.getBody();
            Integer stock = (Integer) body.get("stock");
            Double price = Double.valueOf(body.get("price").toString());
            Long sellerId = Long.valueOf(body.get("sellerId").toString());

            if(stock < it.getQuantity()) throw new RuntimeException("Insufficient stock for product " + it.getProductId());

            String decUrl = ProductServiceUrl + "/products/" + it.getProductId() + "/decrement?quantity=" + it.getQuantity();
            HttpEntity<Void> requestEntity = new HttpEntity<>(new HttpHeaders());
            restTemplate.exchange(decUrl, HttpMethod.POST, requestEntity, Void.class);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(it.getProductId());
            orderItem.setQuantity(it.getQuantity());
            orderItem.setPrice(price);
            orderItem.setSellerId(sellerId);

            items.add(orderItem);
            total += (price * it.getQuantity());
        }

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setItems(items);

        items.forEach(i -> i.setOrder(order));
        Order saved = orderRepository.save(order);
        return saved;
    }

    @Transactional
    public void deleteOrder(Long orderId, Long currentUserId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to delete this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be deleted");
        }

        orderRepository.delete(order);
    }

    @Transactional
    public Order deleteOrderItem(Long orderId, Long itemId, Long currentUserId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to modify this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be modified");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        order.getItems().remove(item);

        if (order.getItems().isEmpty()) {
            orderRepository.delete(order);
            return null;
        }

        double newTotal = order.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setTotalAmount(newTotal);
        return orderRepository.save(order);
    }



    public List<Order> getOrdersByBuyer(Long id){
        return orderRepository.findByBuyerId(id);
    }

    public List<Order> getOrdersBySeller(Long id){
        List<Order> all = orderRepository.findAll();
        List<Order> result = new ArrayList<>();
        for(Order o: all){
            boolean has = o.getItems().stream().anyMatch(it -> id.equals(it.getSellerId()));
            if(has) result.add(o);
        }
        return result;
    }

    public Order getOrdrById(long id){
        return orderRepository.getById(id);
    }
}
