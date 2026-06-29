package com.tss.FoodApp;

import com.tss.FoodApp.enums.Role;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.exception.AuthenticationException;
import com.tss.FoodApp.factory.ServiceRegistry;
import com.tss.FoodApp.model.Customer;
import com.tss.FoodApp.model.User;
import com.tss.FoodApp.service.AuthService;
import com.tss.FoodApp.ui.AdminMenu;
import com.tss.FoodApp.ui.CustomerMenu;
import com.tss.FoodApp.ui.DeliveryPartnerMenu;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Application entry point.
 * Handles:
 * 1. Background threading initialization (limited to 2 threads as per requirements)
 * 2. Default admin seeding (Lazy loaded)
 * 3. Login/Register menu loop
 * 4. Role-based routing to correct dashboard
 */
public class MainApp {

    // Limited to exactly 2 threads as requested.
    // Can be used for any future background processing (e.g., sending emails, metrics).
    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        // ========== Step 1: Initialize ServiceRegistry ==========
        // Repositories are now lazy-loaded. No data is read from disk here.
        ServiceRegistry registry = ServiceRegistry.getInstance();
        AppLogger.info("Application starting with lazy loading...");

        // ========== Step 2: Seed default admin ==========
        // Because of lazy loading, the admin repo will read the file here 
        // ONLY if it hasn't been read yet.
        registry.getAuthService().seedDefaultAdmin();

        // ========== Step 3: Main application loop ==========
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
                    
                    // Gracefully shutdown the 2 background threads
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

    /**
     * Handle login flow — authenticate and route to role-based menu.
     */
    private static void handleLogin(ServiceRegistry registry) {
        InputUtil.printHeader("LOGIN");
        String username = InputUtil.readString("  Username: ");
        String password = InputUtil.readString("  Password: ");

        try {
            AuthService authService = registry.getAuthService();
            User user = authService.login(username, password);

            InputUtil.printSuccess("Welcome, " + user.getName() + "!");

            // Route to role-specific menu
            switch (user.getRole()) {
                case ADMIN:
                    new AdminMenu(user, registry).show();
                    break;
                case CUSTOMER:
                    new CustomerMenu(user, registry).show();
                    break;
                case DELIVERY_PARTNER:
                    new DeliveryPartnerMenu(user, registry).show();
                    break;
            }
        } catch (AuthenticationException e) {
            InputUtil.printError("Login failed: " + e.getMessage());
        } catch (AppException e) {
            InputUtil.printError(e.getMessage());
        }
    }

    /**
     * Handle customer registration flow.
     */
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