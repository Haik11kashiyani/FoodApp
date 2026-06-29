package com.tss.FoodApp.model;

import com.tss.FoodApp.enums.OrderStatus;
import com.tss.FoodApp.enums.PaymentMode;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String customerId;
    private String customerName;
    private List<CartItem> items;
    private double totalAmount;
    private double discountAmount;
    private double finalAmount;
    private PaymentMode paymentMode;
    private String deliveryPartnerId;
    private String deliveryPartnerName;
    private OrderStatus status;
    private String orderedAt;

    public Order(String id, String customerId, String customerName, List<CartItem> items,
                 double totalAmount, double discountAmount, double finalAmount,
                 PaymentMode paymentMode, String deliveryPartnerId, String deliveryPartnerName) {
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

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public List<CartItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public double getFinalAmount() { return finalAmount; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public String getDeliveryPartnerId() { return deliveryPartnerId; }
    public String getDeliveryPartnerName() { return deliveryPartnerName; }
    public OrderStatus getStatus() { return status; }
    public String getOrderedAt() { return orderedAt; }

    public void setId(String id) { this.id = id; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%s] Customer: %s | Total: Rs. %.2f | Status: %s | Date: %s",
                id, customerName, finalAmount, status, orderedAt);
    }
}