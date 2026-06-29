package com.tss.FoodApp.ui;

import com.tss.FoodApp.enums.OrderStatus;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.factory.ServiceRegistry;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.InputUtil;

import java.util.List;

/**
 *DELIVERY PARTNER DASHBOARD — view assigned orders, update status.
 */
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
            InputUtil.printHeader("DELIVERY PARTNER DASHBOARD");
            System.out.println("  Welcome, " + driver.getName() + "!");
            System.out.println("  Status: " + (driver.isAvailable() ? "Available" : "Busy"));
            InputUtil.printDivider();
            InputUtil.printMenuOption(1, "View Assigned Orders");
            InputUtil.printMenuOption(2, "Update Order Status");
            InputUtil.printMenuOption(3, "View Delivery History");
            InputUtil.printMenuOption(4, "Toggle My Availability");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(5, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1: viewAssignedOrders(); break;
                    case 2: updateOrderStatus(); break;
                    case 3: viewDeliveryHistory(); break;
                    case 4: toggleAvailability(); break;
                    case 5:
                        InputUtil.printSuccess("Logged out successfully.");
                        running = false;
                        break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void viewAssignedOrders() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            InputUtil.printWarning("No active orders assigned to you.");
            return;
        }
        InputUtil.printNumberedList(activeOrders, "YOUR ACTIVE ORDERS");
    }

    private void updateOrderStatus() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            InputUtil.printWarning("No active orders to update.");
            return;
        }
        InputUtil.printNumberedList(activeOrders, "ACTIVE ORDERS");

        String orderId = InputUtil.readString("  Enter order ID to update: ");
        System.out.println("  Select new status:");
        System.out.println("  1. PREPARING");
        System.out.println("  2. OUT_FOR_DELIVERY");
        System.out.println("  3. DELIVERED");
        int statusChoice = InputUtil.readInt("  Status: ", 1, 3);

        OrderStatus newStatus;
        switch (statusChoice) {
            case 1: newStatus = OrderStatus.PREPARING; break;
            case 2: newStatus = OrderStatus.OUT_FOR_DELIVERY; break;
            case 3: newStatus = OrderStatus.DELIVERED; break;
            default: newStatus = OrderStatus.PLACED; break;
        }

        Order updated = orderService.updateOrderStatus(orderId, newStatus);
        InputUtil.printSuccess("Order " + orderId + " updated to: " + newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            InputUtil.printSuccess("Delivery completed! You are nowAvailablefor new orders.");
        }
    }

    private void viewDeliveryHistory() {
        List<Order> history = orderService.getOrdersByDriver(driver.getId());
        if (history.isEmpty()) {
            InputUtil.printWarning("No delivery history.");
            return;
        }
        InputUtil.printNumberedList(history, "DELIVERY HISTORY");
    }

    private void toggleAvailability() {
        driver.setAvailable(!driver.isAvailable());
        userService.updateDriver(driver);
        String status = driver.isAvailable() ? "Available" : "UnAvailable";
        InputUtil.printSuccess("Your status is now: " + status);
    }
}