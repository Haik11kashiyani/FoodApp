package com.tss.FoodApp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CONTAINER CLASS FOR ALL DATA MODELS AND ENUMS
 */
public class Models {
    private Models() {} // Dummy class, not instantiated
}

// ==================== ENUMS ====================

enum Role {
    ADMIN,
    CUSTOMER,
    DELIVERY_PARTNER
}

enum OrderStatus {
    PLACED(1),
    PREPARING(2),
    OUT_FOR_DELIVERY(3),
    DELIVERED(4),
    CANCELLED(5);

    private final int step;

    OrderStatus(int step) {
        this.step = step;
    }

    /**
     * Validates if transitioning to the given status is allowed.
     * Rules:
     *   - DELIVERED and CANCELLED are terminal (no further changes)
     *   - Can always cancel an active order
     *   - Must follow sequence: PLACED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (this == DELIVERED || this == CANCELLED) return false;
        if (next == CANCELLED) return true;
        return next.step == this.step + 1;
    }
}

enum FoodCategory {
    VEG,
    NON_VEG
}

enum CuisineType {
    INDIAN,
    ITALIAN
}

enum PaymentMode {
    CASH,
    UPI
}

// ==================== USER MODELS ====================

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String password;
    private String name;
    private Role role;
    private boolean isActive;
    private String createdAt;

    public User(String id, String username, String password, String name, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }
    public String getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s", id, name, username, role, isActive ? "Active" : "Inactive");
    }
}

class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String username, String password, String name) {
        super(id, username, password, name, Role.ADMIN);
    }
}

class Customer extends User {
    private static final long serialVersionUID = 1L;

    private String address;
    private String phone;

    public Customer(String id, String username, String password, String name, String phone, String address) {
        super(id, username, password, name, Role.CUSTOMER);
        this.phone = phone;
        this.address = address;
    }

    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return super.toString() + " | Phone: " + phone + " | Address: " + address;
    }
}

class DeliveryPartner extends User {
    private static final long serialVersionUID = 1L;

    private String vehicleType;
    private boolean isAvailable;

    public DeliveryPartner(String id, String username, String password, String name, String phone, String vehicleType) {
        super(id, username, password, name, Role.DELIVERY_PARTNER);
        this.vehicleType = vehicleType;
        this.isAvailable = true;
    }

    public String getVehicleType() { return vehicleType; }
    public boolean isAvailable() { return isAvailable; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return super.toString() + " | Vehicle: " + vehicleType + " | Available: " + (isAvailable ? "Yes" : "No");
    }
}

// ==================== FOOD & ORDER MODELS ====================

class MenuItem implements Serializable {
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

class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String menuItemId;
    private String itemName;
    private double price;
    private int quantity;

    public CartItem(String menuItemId, String itemName, double price, int quantity) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return String.format("%-20s | Rs. %-8.2f | Qty: %-3d | Subtotal: Rs. %.2f", itemName, price, quantity, getSubtotal());
    }
}

class Order implements Serializable {
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
