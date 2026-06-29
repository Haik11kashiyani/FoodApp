package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.exception.PaymentException;
import com.tss.FoodApp.util.AppLogger;

public class OrderProcessingFacade {
    private final CartService cartService;
    private final OrderService orderService;
    private final Map<PaymentMode, IPaymentStrategy> paymentStrategies;
    private DiscountStrategy discountStrategy;

    public OrderProcessingFacade(CartService cartService, OrderService orderService,
                                 Map<PaymentMode, IPaymentStrategy> paymentStrategies,
                                 DiscountStrategy discountStrategy) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentStrategies = paymentStrategies;
        this.discountStrategy = discountStrategy;
    }

    public Order placeOrder(String customerId, String customerName, PaymentMode paymentMode) {
        if (cartService.isCartEmpty(customerId)) {
            throw new ValidationException("Cart is empty! Add items before placing an order.");
        }

        List<CartItem> items = new ArrayList<>(cartService.getCart(customerId));
        double totalAmount = cartService.getCartTotal(customerId);

        // Apply discount (OCP)
        double discountAmount = discountStrategy.calculateDiscount(totalAmount);
        if (discountAmount > 0) {
            System.out.println("\n  " + discountStrategy.getDescription());
            System.out.printf("   You save: Rs. %.2f%n", discountAmount);
        }
        double finalAmount = totalAmount - discountAmount;

        // Process payment (OCP)
        IPaymentStrategy paymentStrategy = paymentStrategies.get(paymentMode);
        if (paymentStrategy == null) {
            throw new PaymentException("Unsupported payment mode: " + paymentMode);
        }
        paymentStrategy.processPayment(finalAmount);

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

    public DiscountStrategy getDiscountStrategy() { return discountStrategy; }

    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
        AppLogger.info("Discount strategy updated: " + strategy.getDescription());
    }
}
