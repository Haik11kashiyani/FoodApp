package com.tss.FoodApp;

import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.*;
import com.tss.FoodApp.service.AuthService;
import com.tss.FoodApp.ui.AdminMenu;
import com.tss.FoodApp.ui.CustomerMenu;
import com.tss.FoodApp.ui.DeliveryPartnerMenu;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;
import com.tss.FoodApp.config.AppConfig;

public class AppFacade {

    public void start() {
        ServiceRegistry registry = new ServiceRegistry();
        boolean adminSeeded = registry.getAuthService().seedDefaultAdmin();
        if (adminSeeded) {
            System.out.println("  Default admin created. Username: " + AppConfig.DEFAULT_ADMIN_USERNAME
                    + " | Password: " + AppConfig.DEFAULT_ADMIN_PASSWORD);
        }

        AppLogger.info("Application started successfully.");

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
    }

    private void handleLogin(ServiceRegistry registry) {
        System.out.println("\n=== LOGIN ===");
        String username = InputUtil.readString("Username: ");
        String password = InputUtil.readString("Password: ");

        try {
            AuthService authService = registry.getAuthService();
            User user = authService.login(username, password);

            System.out.println("Welcome, " + user.getName() + "!");

            switch (user.getRole()) {
                case ADMIN:
                    AdminMenu adminMenu = new AdminMenu(user, registry);
                    adminMenu.show();
                    break;
                case CUSTOMER:
                    CustomerMenu customerMenu=new CustomerMenu(user, registry);
                    customerMenu.show();
                    break;
                case DELIVERY_PARTNER:
                    DeliveryPartnerMenu deliveryPartnerMenu=new DeliveryPartnerMenu(user, registry);
                    deliveryPartnerMenu.show();
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

    private void handleRegistration(ServiceRegistry registry) {
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
