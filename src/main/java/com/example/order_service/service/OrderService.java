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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Value("${product.service.url}")
    private String ProductServiceUrl;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, Long buyerId) {
        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        for (OrderItemRequest it : request.getItems()) {
            String productUrl = ProductServiceUrl + "/products/" + it.getProductId();

            Map body;
            try {
                // RestTemplate will throw an exception if Product Service returns 404/400
                ResponseEntity<Map> resp = restTemplate.getForEntity(productUrl, Map.class);
                body = resp.getBody();
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                // Catching 404 from Product Service
                throw new ResourceNotFoundException("Product not found with ID: " + it.getProductId());
            } catch (Exception e) {
                throw new BadRequestException("Could not verify product: " + it.getProductId());
            }

            if (body == null) throw new ResourceNotFoundException("Product data is empty for ID: " + it.getProductId());

            Integer stock = (Integer) body.get("stock");
            Double price = Double.valueOf(body.get("price").toString());
            Long sellerId = Long.valueOf(body.get("sellerId").toString());

            // Use BadRequestException for business logic failures (insufficient stock)
            if (stock < it.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product " + it.getProductId() +
                        ". Available: " + stock + ", Requested: " + it.getQuantity());
            }

            // Decrement logic
            try {
                String decUrl = ProductServiceUrl + "/products/" + it.getProductId() + "/decrement?quantity=" + it.getQuantity();
                restTemplate.postForEntity(decUrl, null, Void.class);
            } catch (Exception e) {
                throw new BadRequestException("Stock decrement failed for product: " + it.getProductId());
            }

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
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long currentUserId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be deleted");
        }

        for(OrderItem orderItem: order.getItems()){
            incrementProductStock(orderItem.getProductId(), orderItem.getQuantity());
        }

        orderRepository.delete(order);
    }

    @Transactional
    public Order deleteOrderItem(Long orderId, Long itemId, Long currentUserId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyerId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be modified");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        incrementProductStock(item.getProductId(), item.getQuantity());
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

    void incrementProductStock(Long productId, int quantity){
        try{
            String incUrl = ProductServiceUrl + "/products/" + productId + "/increment?quantity=" + quantity;
            restTemplate.postForEntity(incUrl, null, Void.class);
        } catch (Exception e) {
            logger.error("Failed to increase product quantity - {}", String.valueOf(e));
            throw new BadRequestException("Failed to restock product ID: " + productId);
        }
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

    public Order getOrderById(long id){
        return orderRepository.getById(id);
    }
}
