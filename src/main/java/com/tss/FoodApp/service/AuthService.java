package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.*;
import com.tss.FoodApp.util.AppLogger;

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

    public User login(String username, String password) {
        User user = findUserByUsername(username);

        if (user == null) {
            AppLogger.info("Login failed - user not found: " + username);
            throw new AuthenticationException("User not found: " + username);
        }

        if (!user.isActive()) {
            AppLogger.info("Login attempt on inactive account: " + username);
            throw new AuthenticationException("Account is deactivated. Contact admin.");
        }

        if (!user.getPassword().equals(password)) {
            AppLogger.info("Wrong password for user: " + username);
            throw new AuthenticationException("Invalid password.");
        }

        AppLogger.info("User logged in: " + username + " | Role: " + user.getRole());
        return user;
    }

    public Customer registerCustomer(String username, String password, String name, String phone, String address) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        Customer customer = new Customer(null, username, password, name, phone, address);
        customerRepo.save(customer);
        AppLogger.info("New customer registered: " + username + " | Username: " + username);
        return customer;
    }

    public Admin registerAdmin(String username, String password, String name) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        Admin admin = new Admin(null, username, password, name);
        adminRepo.save(admin);
        AppLogger.info("New admin registered: " + username + " | Username: " + username);
        return admin;
    }

    public DeliveryPartner registerDriver(String username, String password, String name, String phone, String vehicleType) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        DeliveryPartner driver = new DeliveryPartner(null, username, password, name, phone, vehicleType);
        driverRepo.save(driver);
        AppLogger.info("New delivery partner registered: " + username + " | Username: " + username);
        return driver;
    }

    public boolean seedDefaultAdmin() {
        if (adminRepo.findAll().isEmpty()) {
            Admin defaultAdmin = new Admin(null, AppConfig.DEFAULT_ADMIN_USERNAME,
                    AppConfig.DEFAULT_ADMIN_PASSWORD, AppConfig.DEFAULT_ADMIN_NAME);
            adminRepo.save(defaultAdmin);
            AppLogger.info("Default admin seeded: " + AppConfig.DEFAULT_ADMIN_USERNAME);
            return true;
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {
        return findUserByUsername(username) != null;
    }

    private User findUserByUsername(String username) {
        for (Admin u : adminRepo.findAll()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        for (Customer u : customerRepo.findAll()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        for (DeliveryPartner u : driverRepo.findAll()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }
}
