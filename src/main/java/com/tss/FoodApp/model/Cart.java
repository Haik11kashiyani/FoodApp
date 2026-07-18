package com.tss.FoodApp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.repository.Identifiable;

public class Cart implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;

    private Long id; // use customerId as id
    private List<CartItem> items = new ArrayList<>();
    private String createdAt;

    public Cart(Long customerId) {
        this.id = customerId;
        this.createdAt = java.time.LocalDateTime.now().toString();
    }

    public Long getId() { return id; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
