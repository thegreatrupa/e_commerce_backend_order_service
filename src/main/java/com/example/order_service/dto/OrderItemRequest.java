package com.example.order_service.dto;

public class OrderItemRequest {
    private Long productId;
    private Integer stock;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return stock;
    }

    public void setQuantity(Integer quantity) {
        this.stock = quantity;
    }
}
