package com.tss.FoodApp.ui;

import java.util.ArrayList;
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
    private final OrderProcessingFacade orderFacade;
    private final List<MenuCommand> commands = new ArrayList<>();

    public AdminMenu(User admin, ServiceRegistry registry) {
        this.admin = admin;
        this.menuService = registry.getMenuService();
        this.userService = registry.getUserService();
        this.authService = registry.getAuthService();
        this.orderService = registry.getOrderService();
        this.orderFacade = registry.getOrderFacade();

        // Register pluggable commands (OCP)
        commands.add(new AbstractMenuCommand("Menu Management") {
            @Override public void execute() { showMenuManagement(); }
        });
        commands.add(new AbstractMenuCommand("Discount Settings") {
            @Override public void execute() { showDiscountSettings(); }
        });
        commands.add(new AbstractMenuCommand("User Management") {
            @Override public void execute() { showUserManagement(); }
        });
        commands.add(new AbstractMenuCommand("Orders Management") {
            @Override public void execute() { showOrdersManagement(); }
        });
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("  Welcome, " + admin.getName() + "!");
            System.out.println("---------------------------------------------");

            for (int i = 0; i < commands.size(); i++) {
                System.out.println((i + 1) + ". " + commands.get(i).getLabel());
            }
            System.out.println("─────────────");
            System.out.println((commands.size() + 1) + ". Logout");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, commands.size() + 1);

            if (choice == commands.size() + 1) {
                System.out.println("Logged out successfully.");
                running = false;
            } else {
                try {
                    commands.get(choice - 1).execute();
                } catch (AppException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void showMenuManagement() {
        InputUtil.runSubMenu("MENU MANAGEMENT",
            new String[]{"Add Menu Item", "Update Menu Item", "Delete Menu Item", "View All Menu Items"},
            new Runnable[]{this::addMenuItem, this::updateMenuItem, this::deleteMenuItem, this::viewAllMenuItems});
    }

    private void showDiscountSettings() {
        InputUtil.runSubMenu("DISCOUNT SETTINGS",
            new String[]{"View Discount Settings", "Update Discount Settings"},
            new Runnable[]{this::viewDiscountSettings, this::updateDiscountSettings});
    }

    private void showUserManagement() {
        InputUtil.runSubMenu("USER MANAGEMENT",
            new String[]{"Add New Admin", "Add Delivery Partner", "View All Customers", "View All Delivery Partners", "View All Admins", "Toggle User Active/Inactive"},
            new Runnable[]{this::addNewAdmin, this::addDeliveryPartner, this::viewAllCustomers, this::viewAllDrivers, this::viewAllAdmins, this::toggleUserStatus});
    }

    private void showOrdersManagement() {
        InputUtil.runSubMenu("ORDERS MANAGEMENT",
            new String[]{"View All Orders"},
            new Runnable[]{this::viewAllOrders});
    }

    private void addMenuItem() {
        System.out.println("\n=== ADD MENU ITEM ===");
        String name = InputUtil.readString("Item name: ");
        double price = InputUtil.readDouble("Price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readEnum("Cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("Category", FoodCategory.class);

        MenuItem item = menuService.addItem(name, price, category, cuisineType);
        System.out.println("Menu item added: " + item.getName() + " | ID: " + item.getId());
    }

    private void updateMenuItem() {
        System.out.println("\n=== UPDATE MENU ITEM ===");
        viewAllMenuItems();
        String itemId = InputUtil.readString("Enter item ID to update: ");

        String name = InputUtil.readString("New name: ");
        double price = InputUtil.readDouble("New price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readEnum("New cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("New category", FoodCategory.class);

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
        DiscountStrategy strategy = orderFacade.getDiscountStrategy();
        System.out.println("Current Strategy: " + strategy.getDescription());
        System.out.println("Rule: Orders above the minimum amount get the discount automatically.");
    }

    private void updateDiscountSettings() {
        viewDiscountSettings();
        double percentage = InputUtil.readDouble("New discount percentage (1-50): ", 1, 50);
        double threshold = InputUtil.readDouble("New minimum order amount: ", 0, 100000);
        orderFacade.setDiscountStrategy(new PercentageDiscount(percentage, threshold));
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
            java.util.Optional<Order> orderOpt = orders.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst();
            if (orderOpt.isPresent()) {
                orderService.printInvoice(orderOpt.get());
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
