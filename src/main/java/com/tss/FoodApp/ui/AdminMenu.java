package com.tss.FoodApp.ui;

import java.util.List;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.InputUtil;
import com.tss.FoodApp.ServiceRegistry;

public class AdminMenu {
    private final User admin;
    private final MenuService menuService;
    private final UserService userService;
    private final AuthService authService;
    private final OrderService orderService;
    private final OrderProcessor orderProcessor;

    public AdminMenu(User admin, ServiceRegistry registry) {
        this.admin = admin;
        this.menuService = registry.getMenuService();
        this.userService = registry.getUserService();
        this.authService = registry.getAuthService();
        this.orderService = registry.getOrderService();
        this.orderProcessor = registry.getOrderProcessor();
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("  Welcome, " + admin.getName() + "!");
            System.out.println("---------------------------------------------");
            System.out.println("1. Menu Management");
            System.out.println("2. Discount Settings");
            System.out.println("3. User Management");
            System.out.println("4. Orders Management");
            System.out.println("5. Logout");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1:
                        showMenuManagement();
                        break;
                    case 2:
                        showDiscountSettings();
                        break;
                    case 3:
                        showUserManagement();
                        break;
                    case 4:
                        showOrdersManagement();
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

    private void showMenuManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== MENU MANAGEMENT ===");
            System.out.println("1. Add Menu Item");
            System.out.println("2. Update Menu Item");
            System.out.println("3. Delete Menu Item");
            System.out.println("4. View All Menu Items");
            System.out.println("5. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 5);
            try {
                switch (choice) {
                    case 1:
                        addMenuItem();
                        break;
                    case 2:
                        updateMenuItem();
                        break;
                    case 3:
                        deleteMenuItem();
                        break;
                    case 4:
                        viewAllMenuItems();
                        break;
                    case 5:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showDiscountSettings() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== DISCOUNT SETTINGS ===");
            System.out.println("1. View Discount Settings");
            System.out.println("2. Update Discount Settings");
            System.out.println("3. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 3);
            try {
                switch (choice) {
                    case 1:
                        viewDiscountSettings();
                        break;
                    case 2:
                        updateDiscountSettings();
                        break;
                    case 3:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showUserManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== USER MANAGEMENT ===");
            System.out.println("1. Add New Admin");
            System.out.println("2. Add Delivery Partner");
            System.out.println("3. View All Customers");
            System.out.println("4. View All Delivery Partners");
            System.out.println("5. View All Admins");
            System.out.println("6. Toggle User Active/Inactive");
            System.out.println("7. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 7);
            try {
                switch (choice) {
                    case 1:
                        addNewAdmin();
                        break;
                    case 2:
                        addDeliveryPartner();
                        break;
                    case 3:
                        viewAllCustomers();
                        break;
                    case 4:
                        viewAllDrivers();
                        break;
                    case 5:
                        viewAllAdmins();
                        break;
                    case 6:
                        toggleUserStatus();
                        break;
                    case 7:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showOrdersManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== ORDERS MANAGEMENT ===");
            System.out.println("1. View All Orders");
            System.out.println("2. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 2);
            try {
                switch (choice) {
                    case 1:
                        viewAllOrders();
                        break;
                    case 2:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addMenuItem() {
        System.out.println("\n=== ADD MENU ITEM ===");
        String name = InputUtil.readString("Item name: ");
        double price = InputUtil.readDouble("Price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readCuisineType("Cuisine");
        FoodCategory category = InputUtil.readFoodCategory("Category");

        MenuItem item = menuService.addItem(name, price, category, cuisineType);
        System.out.println("Menu item added: " + item.getName() + " | ID: " + item.getId());
    }

    private void updateMenuItem() {
        System.out.println("\n=== UPDATE MENU ITEM ===");
        viewAllMenuItems();
        String itemId = InputUtil.readString("Enter item ID to update: ");

        String name = InputUtil.readString("New name: ");
        double price = InputUtil.readDouble("New price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readCuisineType("New cuisine");
        FoodCategory category = InputUtil.readFoodCategory("New category");

        MenuItem updated = menuService.updateItem(itemId, name, price, category, cuisineType);
        System.out.println("Updated: " + updated.getName());
    }

    private void deleteMenuItem() {
        System.out.println("\n=== DELETE MENU ITEM ===");
        viewAllMenuItems();
        String itemId = InputUtil.readString("Enter item ID to delete: ");

        if (InputUtil.readYesNo("Are you sure?")) {
            menuService.deleteItem(itemId);
            System.out.println("Menu item deleted.");
        }
    }

    private void viewAllMenuItems() {
        List<MenuItem> items = menuService.getAllItems();
        printItemsList(items, "MENU ITEMS");
    }

    private void viewDiscountSettings() {
        System.out.println("\n=== DISCOUNT SETTINGS ===");
        PercentageDiscount strategy = orderProcessor.getDiscountStrategy();
        System.out.println("Current Strategy: " + strategy.getDescription());
        System.out.println("Rule: Orders above the minimum amount get the discount automatically.");
    }

    private void updateDiscountSettings() {
        viewDiscountSettings();
        double percentage = InputUtil.readDouble("New discount percentage (1-50): ", 1, 50);
        double threshold = InputUtil.readDouble("New minimum order amount: ", 0, 100000);
        orderProcessor.setDiscountStrategy(new PercentageDiscount(percentage, threshold));
        System.out.println("Discount settings updated!");
    }

    private void addNewAdmin() {
        System.out.println("\n=== ADD NEW ADMIN ===");
        String username = InputUtil.readValidUsername("Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("Password: ");
        if (password == null) return;
        String name = InputUtil.readString("Full name: ");

        Admin newAdmin = authService.registerAdmin(username, password, name);
        System.out.println("Admin created: " + newAdmin.getName() + " | ID: " + newAdmin.getId());
    }

    private void addDeliveryPartner() {
        System.out.println("\n=== ADD DELIVERY PARTNER ===");
        String username = InputUtil.readValidUsername("Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("Password: ");
        if (password == null) return;
        String name = InputUtil.readString("Full name: ");
        String phone = InputUtil.readValidPhone("Phone: ");
        if (phone == null) return;
        String vehicleType = InputUtil.readString("Vehicle type (Bike/Bicycle/Car): ");

        DeliveryPartner driver = authService.registerDriver(username, password, name, phone, vehicleType);
        System.out.println("Delivery partner created: " + driver.getName() + " | ID: " + driver.getId());
    }

    private void viewAllCustomers() {
        List<Customer> customers = userService.getAllCustomers();
        printItemsList(customers, "CUSTOMERS");
    }

    private void viewAllDrivers() {
        List<DeliveryPartner> drivers = userService.getAllDrivers();
        printItemsList(drivers, "DELIVERY PARTNERS");
    }

    private void viewAllAdmins() {
        List<Admin> admins = userService.getAllAdmins();
        printItemsList(admins, "ADMINS");
    }

    private void toggleUserStatus() {
        System.out.println("\n=== TOGGLE USER STATUS ===");
        List<User> allUsers = userService.getAllUsers();
        printItemsList(allUsers, "ALL USERS");
        String userId = InputUtil.readString("Enter user ID to toggle: ");
        String result = userService.toggleUserStatus(userId);
        System.out.println(result);
    }

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            System.out.println("Warning: No orders found.");
            return;
        }
        printItemsList(orders, "ALL ORDERS");

        if (InputUtil.readYesNo("View order details?")) {
            String orderId = InputUtil.readString("Enter order ID: ");
            Order foundOrder = null;
            for (Order o : orders) {
                if (o.getId().equals(orderId)) {
                    foundOrder = o;
                    break;
                }
            }
            if (foundOrder != null) {
                InvoicePrinter.printInvoice(foundOrder);
            } else {
                System.out.println("Error: Order not found.");
            }
        }
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
