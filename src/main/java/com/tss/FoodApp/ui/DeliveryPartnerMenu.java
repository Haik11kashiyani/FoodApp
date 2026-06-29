package com.tss.FoodApp.ui;

import java.util.ArrayList;
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
    private final List<MenuCommand> commands = new ArrayList<>();

    public DeliveryPartnerMenu(User user, ServiceRegistry registry) {
        this.driver = (DeliveryPartner) user;
        this.orderService = registry.getOrderService();
        this.userService = registry.getUserService();

        // Register pluggable commands (OCP)
        commands.add(new AbstractMenuCommand("View Assigned Orders") {
            @Override public void execute() { viewAssignedOrders(); }
        });
        commands.add(new AbstractMenuCommand("Update Order Status") {
            @Override public void execute() { updateOrderStatus(); }
        });
        commands.add(new AbstractMenuCommand("View Delivery History") {
            @Override public void execute() { viewDeliveryHistory(); }
        });
        commands.add(new AbstractMenuCommand("Toggle My Availability") {
            @Override public void execute() { toggleAvailability(); }
        });
    }

    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("DELIVERY PARTNER DASHBOARD");
            System.out.println("  Welcome, " + driver.getName() + "!");
            System.out.println("  Status: " + (driver.isAvailable() ? "Available" : "Unavailable"));
            InputUtil.printDivider();

            for (int i = 0; i < commands.size(); i++) {
                InputUtil.printMenuOption(i + 1, commands.get(i).getLabel());
            }
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(commands.size() + 1, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, commands.size() + 1);

            if (choice == commands.size() + 1) {
                InputUtil.printSuccess("Logged out successfully.");
                running = false;
            } else {
                try {
                    commands.get(choice - 1).execute();
                } catch (AppException e) {
                    InputUtil.printError(e.getMessage());
                }
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

        orderService.updateOrderStatus(orderId, newStatus);
        InputUtil.printSuccess("Order " + orderId + " updated to: " + newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            InputUtil.printSuccess("Delivery completed! You are now Available for new orders.");
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
        String status = driver.isAvailable() ? "Available" : "Unavailable";
        InputUtil.printSuccess("Your status is now: " + status);
    }
}
