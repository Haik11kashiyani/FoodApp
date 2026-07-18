package com.tss.FoodApp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tss.FoodApp.repository.Identifiable;

public class Order implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long customerId;
    private String customerName;
    private List<CartItem> items;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private PaymentMode paymentMode;
    private Long deliveryPartnerId;
    private String deliveryPartnerName;
    private OrderStatus status;
    private String orderedAt;

    public Order(Long id, Long customerId, String customerName, List<CartItem> items,
                 double totalAmount, double discountAmount, double finalAmount,
                 PaymentMode paymentMode, Long deliveryPartnerId, String deliveryPartnerName) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMode = paymentMode;
        this.deliveryPartnerId = deliveryPartnerId;
        this.deliveryPartnerName = deliveryPartnerName;
        this.status = OrderStatus.PLACED;
        this.orderedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public List<CartItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public Long getDeliveryPartnerId() { return deliveryPartnerId; }
    public String getDeliveryPartnerName() { return deliveryPartnerName; }
    public OrderStatus getStatus() { return status; }
    public String getOrderedAt() { return orderedAt; }

    public void setId(Long id) { this.id = id; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setItems(java.util.List<CartItem> items) { this.items = items; }

    @Override
    public String toString() {
        return String.format("[%s] Customer: %s | Total: Rs. %.2f | Status: %s | Date: %s",
                id, customerName, finalAmount, status, orderedAt);
    }
}
