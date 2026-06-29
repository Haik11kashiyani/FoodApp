package com.tss.FoodApp.service;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.enums.Role;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.exception.AuthenticationException;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handles all authentication: login, registration, and default admin seeding.
 * SRP: This class ONLY handles auth — user CRUD is in UserService.
 */
public class AuthService {

    private final Repository<Admin> adminRepo;
    private final Repository<Customer> customerRepo;
    private final Repository<DeliveryPartner> driverRepo;

    public AuthService(Repository<Admin> adminRepo, Repository<Customer> customerRepo,
                       Repository<DeliveryPartner> driverRepo) {
        this.adminRepo = adminRepo;
        this.customerRepo = customerRepo;
        this.driverRepo = driverRepo;
    }

    /**
     * Authenticate a user by username and password.
     * Searches across all user types (admin, customer, driver).
     * Why search all repos? → Username must be unique globally.
     */
    public User login(String username, String password) {
        // Search in all repos using Stream API
        Optional<User> user = findUserByUsername(username);

        if (!user.isPresent()) {
            AppLogger.info("Login failed - user not found: " + username);
            throw new AuthenticationException("User not found: " + username);
        }

        User foundUser = user.get();

        // Check if account is active
        if (!foundUser.isActive()) {
            AppLogger.info("Login attempt on inactive account: " + username);
            throw new AuthenticationException("Account is deactivated. Contact admin.");
        }

        // Verify password (plain text comparison)
        if (!foundUser.getPassword().equals(password)) {
            AppLogger.info("Wrong password for user: " + username);
            throw new AuthenticationException("Invalid password.");
        }

        AppLogger.info("User logged in: " + username + " | Role: " + foundUser.getRole());
        return foundUser;
    }

    /**
     * Register a new customer.
     * Validates username uniqueness across ALL user types.
     */
    public Customer registerCustomer(String username, String password, String name, String phone, String address) {
        // Check uniqueness
        if (isUsernameTaken(username)) {
            throw new AppException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        Customer customer = new Customer(id, username, password, name, phone, address);
        customerRepo.save(customer);
        AppLogger.info("New customer registered: " + username + " | ID: " + id);
        return customer;
    }

    /**
     * Register a new admin (only existing admin can do this).
     */
    public Admin registerAdmin(String username, String password, String name) {
        if (isUsernameTaken(username)) {
            throw new AppException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        Admin admin = new Admin(id, username, password, name);
        adminRepo.save(admin);
        AppLogger.info("New admin registered: " + username + " | ID: " + id);
        return admin;
    }

    /**
     * Register a new delivery partner (admin does this).
     */
    public DeliveryPartner registerDriver(String username, String password, String name, String phone, String vehicleType) {
        if (isUsernameTaken(username)) {
            throw new AppException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        DeliveryPartner driver = new DeliveryPartner(id, username, password, name, phone, vehicleType);
        driverRepo.save(driver);
        AppLogger.info("New delivery partner registered: " + username + " | ID: " + id);
        return driver;
    }

    /**
     * Seeds the default admin if no admins exist (first run).
     */
    public void seedDefaultAdmin() {
        if (adminRepo.findAll().isEmpty()) {
            String id = IdGenerator.generateId();
            Admin defaultAdmin = new Admin(id, AppConfig.DEFAULT_ADMIN_USERNAME,
                    AppConfig.DEFAULT_ADMIN_PASSWORD, AppConfig.DEFAULT_ADMIN_NAME);
            adminRepo.save(defaultAdmin);
            AppLogger.info("Default admin seeded: " + AppConfig.DEFAULT_ADMIN_USERNAME);
            System.out.println("  Default admin created. Username: " + AppConfig.DEFAULT_ADMIN_USERNAME
                    + " | Password: " + AppConfig.DEFAULT_ADMIN_PASSWORD);
        }
    }

    /**
     * Check if a username is already taken across all user types.
     * Uses Stream API to search efficiently.
     */
    public boolean isUsernameTaken(String username) {
        return findUserByUsername(username).isPresent();
    }

    /**
     * Find a user by username across all repositories.
     * Uses Stream.concat to merge all user lists and search.
     */
    private Optional<User> findUserByUsername(String username) {
        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(adminRepo.findAll());
        allUsers.addAll(customerRepo.findAll());
        allUsers.addAll(driverRepo.findAll());

        return allUsers.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}