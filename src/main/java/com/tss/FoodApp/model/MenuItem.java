package com.tss.FoodApp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tss.FoodApp.repository.Identifiable;

public class MenuItem implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private double price;
    private FoodCategory category;
    private CuisineType cuisineType;
    private boolean isAvailable;
    private String createdAt;

    public MenuItem(String id, String name, double price, FoodCategory category, CuisineType cuisineType) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.cuisineType = cuisineType;
        this.isAvailable = true;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public MenuItem(String id, String name, double price, FoodCategory category, CuisineType cuisineType, boolean isAvailable) {
        this(id, name, price, category, cuisineType);
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public FoodCategory getCategory() { return category; }
    public CuisineType getCuisineType() {
        return cuisineType == null ? CuisineType.INDIAN : cuisineType;
    }
    public boolean isAvailable() { return isAvailable; }
    public String getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(FoodCategory category) { this.category = category; }
    public void setCuisineType(CuisineType cuisineType) { this.cuisineType = cuisineType; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Rs. %-8.2f | %-8s | %-8s | %s", id, name, price, category, getCuisineType(), isAvailable ? "Available" : "Unavailable");
    }
}
