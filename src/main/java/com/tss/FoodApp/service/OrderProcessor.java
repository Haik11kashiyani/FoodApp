package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.exception.PaymentException;
import com.tss.FoodApp.util.AppLogger;

public class OrderProcessor {
    private final CartService cartService;
    private final OrderService orderService;
    private PercentageDiscount discountStrategy;

    public OrderProcessor(CartService cartService, OrderService orderService,
                          PercentageDiscount discountStrategy) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.discountStrategy = discountStrategy;
    }

    public Order placeOrder(String customerId, String customerName, PaymentMode paymentMode, String upiId) {
        if (cartService.isCartEmpty(customerId)) {
            throw new ValidationException("Cart is empty! Add items before placing an order.");
        }

        List<CartItem> items = new ArrayList<>(cartService.getCart(customerId));
        double totalAmount = cartService.getCartTotal(customerId);

        // Apply discount directly
        double discountAmount = discountStrategy.calculateDiscount(totalAmount);
        double finalAmount = totalAmount - discountAmount;

        // Process payment directly
        processPayment(paymentMode, finalAmount, upiId);

        // Assign driver and create order
        DeliveryPartner driver = orderService.assignRandomDriver();
        Order order = orderService.createOrder(customerId, customerName, items,
                totalAmount, discountAmount, finalAmount, paymentMode,
                driver.getId(), driver.getName());

        cartService.clearCart(customerId);

        AppLogger.info("Order placed successfully | Order ID: " + order.getId());
        return order;
    }

    private void processPayment(PaymentMode mode, double amount, String upiId) {
        if (mode == PaymentMode.CASH) {
            AppLogger.info("Cash payment processed for Rs. " + String.format("%.2f", amount));
        } else if (mode == PaymentMode.UPI) {
            if (upiId == null || !upiId.contains("@")) {
                AppLogger.warn("Invalid UPI ID entered: " + upiId);
                throw new PaymentException("Invalid UPI ID format. Must contain '@'.");
            }
            AppLogger.info("UPI payment processed for Rs. " + String.format("%.2f", amount) + " via " + upiId);
        } else {
            throw new PaymentException("Unsupported payment mode: " + mode);
        }
    }

    public PercentageDiscount getDiscountStrategy() {
        return discountStrategy;
    }

    public void setDiscountStrategy(PercentageDiscount strategy) {
        this.discountStrategy = strategy;
        AppLogger.info("Discount strategy updated: " + strategy.getDescription());
    }
}
