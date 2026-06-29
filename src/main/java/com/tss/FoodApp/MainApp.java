package com.tss.FoodApp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.*;
import com.tss.FoodApp.service.AuthService;
import com.tss.FoodApp.ui.DashboardFactory;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;

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
        if (username == null) return;

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
