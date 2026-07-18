package com.tss.FoodApp.ui;

import java.util.List;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.service.OrderService;
import com.tss.FoodApp.service.UserService;
import com.tss.FoodApp.util.InputUtil;
import com.tss.FoodApp.ServiceRegistry;

public class DeliveryPartnerMenu {
    private final DeliveryPartner driver;
    private final OrderService orderService;
    private final UserService userService;

    public DeliveryPartnerMenu(User user, ServiceRegistry registry) {
        this.driver = (DeliveryPartner) user;
        this.orderService = registry.getOrderService();
        this.userService = registry.getUserService();
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== DELIVERY PARTNER DASHBOARD ===");
            System.out.println("  Welcome, " + driver.getName() + "!");
            System.out.println("  Status: " + (driver.isAvailable() ? "Available" : "Unavailable"));
            System.out.println("---------------------------------------------");
            System.out.println("1. View Assigned Orders");
            System.out.println("2. Update Order Status");
            System.out.println("3. View Delivery History");
            System.out.println("4. Toggle My Availability");
            System.out.println("5. Logout");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1:
                        viewAssignedOrders();
                        break;
                    case 2:
                        updateOrderStatus();
                        break;
                    case 3:
                        viewDeliveryHistory();
                        break;
                    case 4:
                        toggleAvailability();
                        break;
                    case 5:
                        System.out.println("Logged out successfully.");
                        running = false;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void viewAssignedOrders() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            System.out.println("Warning: No active orders assigned to you.");
            return;
        }
        printItemsList(activeOrders, "YOUR ACTIVE ORDERS");
    }

    private void updateOrderStatus() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            System.out.println("Warning: No active orders to update.");
            return;
        }
        printItemsList(activeOrders, "ACTIVE ORDERS");

        Long orderId = InputUtil.readLong("Enter order ID to update: ");
        System.out.println("Select new status:");
        System.out.println("1. PREPARING");
        System.out.println("2. OUT_FOR_DELIVERY");
        System.out.println("3. DELIVERED");
        int statusChoice = InputUtil.readInt("Status: ", 1, 3);

        OrderStatus newStatus;
        switch (statusChoice) {
            case 1: newStatus = OrderStatus.PREPARING; break;
            case 2: newStatus = OrderStatus.OUT_FOR_DELIVERY; break;
            case 3: newStatus = OrderStatus.DELIVERED; break;
            default: newStatus = OrderStatus.PLACED; break;
        }

        orderService.updateOrderStatus(orderId, newStatus);
        System.out.println("Order " + orderId + " updated to: " + newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            System.out.println("Delivery completed! You are now Available for new orders.");
        }
    }

    private void viewDeliveryHistory() {
        List<Order> history = orderService.getOrdersByDriver(driver.getId());
        if (history.isEmpty()) {
            System.out.println("Warning: No delivery history.");
            return;
        }
        printItemsList(history, "DELIVERY HISTORY");
    }

    private void toggleAvailability() {
        driver.setAvailable(!driver.isAvailable());
        userService.updateDriver(driver);
        String status = driver.isAvailable() ? "Available" : "Unavailable";
        System.out.println("Your status is now: " + status);
    }

    private <T> void printItemsList(List<T> items, String title) {
        if (items.isEmpty()) {
            System.out.println("Warning: No " + title.toLowerCase() + " found.");
            return;
        }
        System.out.println("---------------------------------------------");
        System.out.println(title + " (" + items.size() + " items)");
        System.out.println("---------------------------------------------");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).toString());
        }
        System.out.println("---------------------------------------------");
    }
}
