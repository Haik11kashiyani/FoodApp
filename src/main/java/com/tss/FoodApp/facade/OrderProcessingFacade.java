package com.tss.FoodApp.facade;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.enums.PaymentMode;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * FACADE PATTERN — The most important design pattern in this project.
 *
 * WHY FACADE?
 * Placing an order involves 5 steps across multiple services:
 *   1. Get cart items (CartService)
 *   2. Calculate discount (internal logic)
 *   3. Process payment (IPaymentStrategy via Strategy pattern)
 *   4. Assign delivery partner (OrderService)
 *   5. Create order + print invoice (OrderService)
 *
 * Without Facade: CustomerMenu would need references to CartService, OrderService,
 * and payment strategy classes. It would call 5+ methods in sequence — 20+ lines of
 * orchestration logic mixed with UI code.
 *
 * With Facade: CustomerMenu calls ONE method → placeOrder(). All complexity is hidden.
 *
 * WHEN TO USE FACADE: When a single user action ("place order") requires coordinating
 * multiple subsystems. Facade simplifies the interface for the caller.
 *
 * Alternative: Put all logic in CustomerMenu directly.
 * Rejected: Violates SRP — menu should handle UI, not business orchestration.
 */
public class OrderProcessingFacade {

    private final CartService cartService;
    private final OrderService orderService;
    private final CashPayment cashPayment;
    private final UpiPayment upiPayment;

    // Discount settings (admin can modify these at runtime)
    private double discountPercentage = AppConfig.DEFAULT_DISCOUNT_PERCENTAGE;
    private double discountThreshold = AppConfig.DEFAULT_DISCOUNT_THRESHOLD;

    public OrderProcessingFacade(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.cashPayment = new CashPayment();
        this.upiPayment = new UpiPayment();
    }

    /**
     * THE FACADE METHOD — Orchestrates the entire order placement workflow.
     *
     * @param customerId   ID of the customer placing the order
     * @param customerName Name of the customer (for invoice)
     * @param paymentMode  CASH or UPI
     * @return The created Order object
     */
    public Order placeOrder(String customerId, String customerName, PaymentMode paymentMode) {
        // Step 1: Validate cart is not empty
        if (cartService.isCartEmpty(customerId)) {
            throw new AppException("Cart is empty! Add items before placing an order.");
        }

        // Step 2: Get cart items and calculate total
        List<CartItem> items = new ArrayList<>(cartService.getCart(customerId));
        double totalAmount = cartService.getCartTotal(customerId);

        // Step 3: Calculate discount
        double discountAmount = 0;
        if (totalAmount >= discountThreshold) {
            discountAmount = totalAmount * (discountPercentage / 100.0);
            System.out.printf("\n Discount applied! %.0f%% off on orders above Rs. %.0f%n",
                    discountPercentage, discountThreshold);
            System.out.printf("   You save: Rs. %.2f%n", discountAmount);
        }
        double finalAmount = totalAmount - discountAmount;

        // Step 4: Process payment using Strategy Pattern
        IPaymentStrategy strategy = (paymentMode == PaymentMode.CASH) ? cashPayment : upiPayment;
        boolean paymentSuccess = strategy.processPayment(finalAmount);
        if (!paymentSuccess) {
            throw new AppException("Payment failed. Order not placed.");
        }

        // Step 5: Assign random delivery partner
        DeliveryPartner driver = orderService.assignRandomDriver();

        // Step 6: Create the order
        Order order = orderService.createOrder(customerId, customerName, items,
                totalAmount, discountAmount, finalAmount, paymentMode,
                driver.getId(), driver.getName());

        // Step 7: Print invoice
        orderService.printInvoice(order);

        // Step 8: Clear the cart
        cartService.clearCart(customerId);

        AppLogger.info("Order placed successfully via Facade | Order ID: " + order.getId());
        return order;
    }

    // --- Discount settings management (for admin) ---

    public double getDiscountPercentage() { return discountPercentage; }
    public double getDiscountThreshold() { return discountThreshold; }

    public void setDiscountPercentage(double percentage) {
        this.discountPercentage = percentage;
        AppLogger.info("Discount percentage updated to: " + percentage + "%");
    }

    public void setDiscountThreshold(double threshold) {
        this.discountThreshold = threshold;
        AppLogger.info("Discount threshold updated to: Rs. " + threshold);
    }
}