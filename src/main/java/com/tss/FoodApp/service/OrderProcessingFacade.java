package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.exception.PaymentException;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;

public class OrderProcessingFacade {
    private final CartService cartService;
    private final OrderService orderService;
    private PercentageDiscount discountStrategy;

    public OrderProcessingFacade(CartService cartService, OrderService orderService,
                                 PercentageDiscount discountStrategy) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.discountStrategy = discountStrategy;
    }

    public Order placeOrder(String customerId, String customerName, PaymentMode paymentMode) {
        if (cartService.isCartEmpty(customerId)) {
            throw new ValidationException("Cart is empty! Add items before placing an order.");
        }

        List<CartItem> items = new ArrayList<>(cartService.getCart(customerId));
        double totalAmount = cartService.getCartTotal(customerId);

        // Apply discount directly
        double discountAmount = discountStrategy.calculateDiscount(totalAmount);
        if (discountAmount > 0) {
            System.out.println("\n  " + discountStrategy.getDescription());
            System.out.printf("   You save: Rs. %.2f%n", discountAmount);
        }
        double finalAmount = totalAmount - discountAmount;

        // Process payment directly
        processPayment(paymentMode, finalAmount);

        // Assign driver and create order
        DeliveryPartner driver = orderService.assignRandomDriver();
        Order order = orderService.createOrder(customerId, customerName, items,
                totalAmount, discountAmount, finalAmount, paymentMode,
                driver.getId(), driver.getName());

        orderService.printInvoice(order);
        cartService.clearCart(customerId);

        AppLogger.info("Order placed successfully via Facade | Order ID: " + order.getId());
        return order;
    }

    private void processPayment(PaymentMode mode, double amount) {
        if (mode == PaymentMode.CASH) {
            System.out.println("\n  Cash Payment Selected");
            System.out.printf("   Amount to pay on delivery: Rs. %.2f%n", amount);
            System.out.println("   Status: Payment will be collected on delivery.");
            AppLogger.info("Cash payment processed for Rs. " + String.format("%.2f", amount));
        } else if (mode == PaymentMode.UPI) {
            System.out.println("\n  UPI Payment Selected");
            String upiId = InputUtil.readLine("   Enter your UPI ID (e.g., name@upi): ");

            if (!upiId.contains("@")) {
                AppLogger.warn("Invalid UPI ID entered: " + upiId);
                throw new PaymentException("Invalid UPI ID format. Must contain '@'.");
            }

            System.out.printf("   Processing payment of Rs. %.2f via UPI ID: %s%n", amount, upiId);
            System.out.println("   UPI Payment Successful!");
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
