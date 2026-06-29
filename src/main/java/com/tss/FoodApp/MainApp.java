package com.tss.FoodApp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainApp {

    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        // Set console output to UTF-8 to support nice borders on Windows terminals
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to default
        }

        ServiceRegistry registry = ServiceRegistry.getInstance();
        AppLogger.info("Application starting with lazy loading...");

        // Asynchronously preload all repositories in background using 2 threads
        backgroundExecutor.submit(() -> registry.getAdminRepo().findAll());
        backgroundExecutor.submit(() -> registry.getCustomerRepo().findAll());
        backgroundExecutor.submit(() -> registry.getDriverRepo().findAll());
        backgroundExecutor.submit(() -> registry.getMenuRepo().findAll());
        backgroundExecutor.submit(() -> registry.getOrderRepo().findAll());

        registry.getAuthService().seedDefaultAdmin();

        AppLogger.info("Application started successfully.");
        boolean appRunning = true;

        while (appRunning) {
            InputUtil.printHeader("FOOD ORDERING SYSTEM");
            InputUtil.printMenuOption(1, "Login");
            InputUtil.printMenuOption(2, "Register as Customer");
            InputUtil.printMenuOption(3, "Exit");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, 3);

            switch (choice) {
                case 1:
                    handleLogin(registry);
                    break;
                case 2:
                    handleRegistration(registry);
                    break;
                case 3:
                    InputUtil.printSuccess("Thank you for using Food Ordering System! Goodbye!");
                    AppLogger.info("Application shutting down.");
                    InputUtil.close();

                    backgroundExecutor.shutdown();
                    try {
                        if (!backgroundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                            backgroundExecutor.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        backgroundExecutor.shutdownNow();
                    }

                    appRunning = false;
                    break;
            }
        }
    }

    private static void handleLogin(ServiceRegistry registry) {
        InputUtil.printHeader("LOGIN");
        String username = InputUtil.readString("  Username: ");
        String password = InputUtil.readString("  Password: ");

        try {
            AuthService authService = registry.getAuthService();
            User user = authService.login(username, password);

            InputUtil.printSuccess("Welcome, " + user.getName() + "!");

            DashboardFactory factory = registry.getDashboardFactory(user.getRole());
            if (factory != null) {
                factory.showDashboard(user, registry);
            } else {
                throw new AppException("No dashboard factory registered for role: " + user.getRole());
            }
        } catch (AuthenticationException e) {
            InputUtil.printError("Login failed: " + e.getMessage());
        } catch (AppException e) {
            InputUtil.printError(e.getMessage());
        }
    }

    private static void handleRegistration(ServiceRegistry registry) {
        InputUtil.printHeader("REGISTER AS CUSTOMER");

        String username = InputUtil.readValidUsername("  Choose username: ");
        if (username == null) return;

        String password = InputUtil.readValidPassword("  Choose password: ");
        if (password == null) return;

        String name = InputUtil.readString("  Full name: ");

        String phone = InputUtil.readValidPhone("  Phone number: ");
        if (phone == null) return;

        String address = InputUtil.readLine("  Delivery address: ");

        try {
            Customer customer = registry.getAuthService().registerCustomer(
                    username, password, name, phone, address);
            InputUtil.printSuccess("Registration successful! Your ID: " + customer.getId());
            InputUtil.printSuccess("You can now login with your credentials.");
        } catch (AppException e) {
            InputUtil.printError("Registration failed: " + e.getMessage());
        }
    }
}

// ==================== APP CONFIG ====================

final class AppConfig {
    private AppConfig() {}

    public static final String DATA_DIR = "data";
    public static final String LOG_DIR = "logs";
    public static final String ADMIN_FILE = DATA_DIR + "/admins.dat";
    public static final String CUSTOMER_FILE = DATA_DIR + "/customers.dat";
    public static final String DELIVERY_PARTNER_FILE = DATA_DIR + "/delivery_partners.dat";
    public static final String MENU_FILE = DATA_DIR + "/menu_items.dat";
    public static final String ORDER_FILE = DATA_DIR + "/orders.dat";
    public static final String LOG_FILE = LOG_DIR + "/app.log";

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_ADMIN_NAME = "System Admin";

    public static final double DEFAULT_DISCOUNT_PERCENTAGE = 10.0;
    public static final double DEFAULT_DISCOUNT_THRESHOLD = 500.0;

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final double MAX_PRICE = 10000.0;
    public static final int MAX_QUANTITY = 50;
}

// ==================== CUSTOM EXCEPTIONS ====================
