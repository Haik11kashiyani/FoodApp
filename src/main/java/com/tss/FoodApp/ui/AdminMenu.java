package com.tss.FoodApp.ui;

import com.tss.FoodApp.enums.CuisineType;
import com.tss.FoodApp.enums.FoodCategory;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.facade.OrderProcessingFacade;
import com.tss.FoodApp.factory.ServiceRegistry;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.InputUtil;

import java.util.List;

/**
 *ADMIN DASHBOARD menu — handles all admin operations.
 * Receives the logged-in Admin user and ServiceRegistry via constructor.
 * Why pass registry not individual services? → Cleaner constructor, admin uses many services.
 */
public class AdminMenu {

    private final User admin;
    private final MenuService menuService;
    private final UserService userService;
    private final AuthService authService;
    private final OrderService orderService;
    private final OrderProcessingFacade orderFacade;

    public AdminMenu(User admin, ServiceRegistry registry) {
        this.admin = admin;
        this.menuService = registry.getMenuService();
        this.userService = registry.getUserService();
        this.authService = registry.getAuthService();
        this.orderService = registry.getOrderService();
        this.orderFacade = registry.getOrderFacade();
    }

    /**
     * Main admin menu loop. Keeps showing options until logout.
     */
    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("ADMIN DASHBOARD");
            System.out.println("  Welcome, " + admin.getName() + "!");
            InputUtil.printDivider();
            InputUtil.printMenuOption(1, "Menu Management");
            InputUtil.printMenuOption(2, "Discount Settings");
            InputUtil.printMenuOption(3, "User Management");
            InputUtil.printMenuOption(4, "Orders Management");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(5, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1: showMenuManagement(); break;
                    case 2: showDiscountSettings(); break;
                    case 3: showUserManagement(); break;
                    case 4: showOrdersManagement(); break;
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

    private void showMenuManagement() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("MENU MANAGEMENT");
            InputUtil.printMenuOption(1, "Add Menu Item");
            InputUtil.printMenuOption(2, "Update Menu Item");
            InputUtil.printMenuOption(3, "Delete Menu Item");
            InputUtil.printMenuOption(4, "View All Menu Items");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(5, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 5);
            try {
                switch (choice) {
                    case 1: addMenuItem(); break;
                    case 2: updateMenuItem(); break;
                    case 3: deleteMenuItem(); break;
                    case 4: viewAllMenuItems(); break;
                    case 5: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showDiscountSettings() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("DISCOUNT SETTINGS");
            InputUtil.printMenuOption(1, "View Discount Settings");
            InputUtil.printMenuOption(2, "Update Discount Settings");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(3, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 3);
            try {
                switch (choice) {
                    case 1: viewDiscountSettings(); break;
                    case 2: updateDiscountSettings(); break;
                    case 3: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showUserManagement() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("USER MANAGEMENT");
            InputUtil.printMenuOption(1, "Add New Admin");
            InputUtil.printMenuOption(2, "Add Delivery Partner");
            InputUtil.printMenuOption(3, "View All Customers");
            InputUtil.printMenuOption(4, "View All Delivery Partners");
            InputUtil.printMenuOption(5, "View All Admins");
            InputUtil.printMenuOption(6, "Toggle User Active/Inactive");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(7, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 7);
            try {
                switch (choice) {
                    case 1: addNewAdmin(); break;
                    case 2: addDeliveryPartner(); break;
                    case 3: viewAllCustomers(); break;
                    case 4: viewAllDrivers(); break;
                    case 5: viewAllAdmins(); break;
                    case 6: toggleUserStatus(); break;
                    case 7: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showOrdersManagement() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("ORDERS MANAGEMENT");
            InputUtil.printMenuOption(1, "View All Orders");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(2, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 2);
            try {
                switch (choice) {
                    case 1: viewAllOrders(); break;
                    case 2: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }


    // ==================== Menu Management ====================

    private void addMenuItem() {
        InputUtil.printHeader("ADD MENU ITEM");
        String name = InputUtil.readString("  Item name: ");
        double price = InputUtil.readDouble("  Price: ", 1, 10000);
        CuisineType cuisineType = InputUtil.readEnum("  Cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("  Category", FoodCategory.class);

        MenuItem item = menuService.addItem(name, price, category, cuisineType);
        InputUtil.printSuccess("Menu item added: " + item.getName() + " | ID: " + item.getId());
    }

    private void updateMenuItem() {
        InputUtil.printHeader("UPDATE MENU ITEM");
        viewAllMenuItems();
        String itemId = InputUtil.readString("  Enter item ID to update: ");

        String name = InputUtil.readString("  New name: ");
        double price = InputUtil.readDouble("  New price: ", 1, 10000);
        CuisineType cuisineType = InputUtil.readEnum("  New cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("  New category", FoodCategory.class);

        MenuItem updated = menuService.updateItem(itemId, name, price, category, cuisineType);
        InputUtil.printSuccess("Updated: " + updated.getName());
    }

    private void deleteMenuItem() {
        InputUtil.printHeader("DELETE MENU ITEM");
        viewAllMenuItems();
        String itemId = InputUtil.readString("  Enter item ID to delete: ");

        if (InputUtil.readYesNo("  Are you sure?")) {
            menuService.deleteItem(itemId);
            InputUtil.printSuccess("Menu item deleted.");
        }
    }

    private void viewAllMenuItems() {
        List<MenuItem> items = menuService.getAllItems();
        InputUtil.printNumberedList(items, "MENU ITEMS");
    }

    // ==================== Discount Settings ====================

    private void viewDiscountSettings() {
        InputUtil.printHeader("DISCOUNT SETTINGS");
        System.out.printf("  Current Discount  : %.0f%%%n", orderFacade.getDiscountPercentage());
        System.out.printf("  Min Order Amount  : Rs. %.0f%n", orderFacade.getDiscountThreshold());
        System.out.println("  Rule: Orders above the minimum amount get the discount automatically.");
    }

    private void updateDiscountSettings() {
        viewDiscountSettings();
        double percentage = InputUtil.readDouble("  New discount percentage (1-50): ", 1, 50);
        double threshold = InputUtil.readDouble("  New minimum order amount: ", 0, 100000);
        orderFacade.setDiscountPercentage(percentage);
        orderFacade.setDiscountThreshold(threshold);
        InputUtil.printSuccess("Discount settings updated!");
    }

    // ==================== User Management ====================

    private void addNewAdmin() {
        InputUtil.printHeader("ADD NEW ADMIN");
        String username = InputUtil.readValidUsername("  Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("  Password: ");
        if (password == null) return;
        String name = InputUtil.readString("  Full name: ");

        Admin newAdmin = authService.registerAdmin(username, password, name);
        InputUtil.printSuccess("Admin created: " + newAdmin.getName() + " | ID: " + newAdmin.getId());
    }

    private void addDeliveryPartner() {
        InputUtil.printHeader("ADD DELIVERY PARTNER");
        String username = InputUtil.readValidUsername("  Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("  Password: ");
        if (password == null) return;
        String name = InputUtil.readString("  Full name: ");
        String phone = InputUtil.readValidPhone("  Phone: ");
        if (phone == null) return;
        String vehicleType = InputUtil.readString("  Vehicle type (Bike/Bicycle/Car): ");

        DeliveryPartner driver = authService.registerDriver(username, password, name, phone, vehicleType);
        InputUtil.printSuccess("Delivery partner created: " + driver.getName() + " | ID: " + driver.getId());
    }

    private void viewAllCustomers() {
        List<Customer> customers = userService.getAllCustomers();
        InputUtil.printNumberedList(customers, "CUSTOMERS");
    }

    private void viewAllDrivers() {
        List<DeliveryPartner> drivers = userService.getAllDrivers();
        InputUtil.printNumberedList(drivers, "DELIVERY PARTNERS");
    }

    private void viewAllAdmins() {
        List<Admin> admins = userService.getAllAdmins();
        InputUtil.printNumberedList(admins, "ADMINS");
    }

    private void toggleUserStatus() {
        InputUtil.printHeader("TOGGLE USER STATUS");
        List<User> allUsers = userService.getAllUsers();
        InputUtil.printNumberedList(allUsers, "ALL USERS");
        String userId = InputUtil.readString("  Enter user ID to toggle: ");
        String result = userService.toggleUserStatus(userId);
        InputUtil.printSuccess(result);
    }

    // ==================== Orders ====================

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            InputUtil.printWarning("No orders found.");
            return;
        }
        InputUtil.printNumberedList(orders, "ALL ORDERS");

        if (InputUtil.readYesNo("  View order details?")) {
            String orderId = InputUtil.readString("  Enter order ID: ");
            java.util.Optional<Order> orderOpt = orders.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst();
            if (orderOpt.isPresent()) {
                orderService.printInvoice(orderOpt.get());
            } else {
                InputUtil.printError("Order not found.");
            }
        }
    }
}