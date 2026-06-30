package com.tss.FoodApp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.*;
import com.tss.FoodApp.service.AuthService;
import com.tss.FoodApp.ui.AdminMenu;
import com.tss.FoodApp.ui.CustomerMenu;
import com.tss.FoodApp.ui.DeliveryPartnerMenu;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;

public class MainApp {

    public static void main(String[] args) {
        ServiceRegistry registry = ServiceRegistry.getInstance();
        registry.getAuthService().seedDefaultAdmin();

        AppLogger.info("Application started successfully.");

        ExecutorService mainExecutor = Executors.newSingleThreadExecutor();
        mainExecutor.submit(() -> {
            boolean appRunning = true;
            while (appRunning) {
                System.out.println("\n=== FOOD ORDERING SYSTEM ===");
                System.out.println("1. Login");
                System.out.println("2. Register as Customer");
                System.out.println("3. Exit");

                int choice = InputUtil.readInt("Enter choice: ", 1, 3);

                switch (choice) {
                    case 1:
                        handleLogin(registry);
                        break;
                    case 2:
                        handleRegistration(registry);
                        break;
                    case 3:
                        System.out.println("Thank you for using Food Ordering System! Goodbye!");
                        AppLogger.info("Application shutting down.");
                        InputUtil.close();
                        appRunning = false;
                        break;
                }
            }
        });

        mainExecutor.shutdown();
        try {
            mainExecutor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void handleLogin(ServiceRegistry registry) {
        System.out.println("\n=== LOGIN ===");
        String username = InputUtil.readString("Username: ");
        String password = InputUtil.readString("Password: ");

        try {
            AuthService authService = registry.getAuthService();
            User user = authService.login(username, password);

            System.out.println("Welcome, " + user.getName() + "!");

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
                default:
                    throw new AppException("No dashboard registered for role: " + user.getRole());
            }
        } catch (AuthenticationException e) {
            System.out.println("Error: Login failed: " + e.getMessage());
        } catch (AppException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleRegistration(ServiceRegistry registry) {
        System.out.println("\n=== REGISTER AS CUSTOMER ===");

        String username = InputUtil.readValidUsername("Choose username: ");
        if (username == null) return;

        String password = InputUtil.readValidPassword("Choose password: ");
        if (password == null) return;

        String name = InputUtil.readString("Full name: ");

        String phone = InputUtil.readValidPhone("Phone number: ");
        if (phone == null) return;

        String address = InputUtil.readLine("Delivery address: ");

        try {
            Customer customer = registry.getAuthService().registerCustomer(
                    username, password, name, phone, address);
            System.out.println("Registration successful! Your ID: " + customer.getId());
            System.out.println("You can now login with your credentials.");
        } catch (AppException e) {
            System.out.println("Error: Registration failed: " + e.getMessage());
        }
    }
}
