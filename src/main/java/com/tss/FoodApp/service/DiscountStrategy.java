package com.tss.FoodApp.service;

public interface DiscountStrategy {
    double calculateDiscount(double totalAmount);
    String getDescription();
}
