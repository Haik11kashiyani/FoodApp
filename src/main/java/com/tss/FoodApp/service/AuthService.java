package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.*;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;

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
        Optional<User> user = findUserByUsername(username);

        if (!user.isPresent()) {
            AppLogger.info("Login failed - user not found: " + username);
            throw new AuthenticationException("User not found: " + username);
        }

        User foundUser = user.get();

        if (!foundUser.isActive()) {
            AppLogger.info("Login attempt on inactive account: " + username);
            throw new AuthenticationException("Account is deactivated. Contact admin.");
        }

        if (!foundUser.getPassword().equals(password)) {
            AppLogger.info("Wrong password for user: " + username);
            throw new AuthenticationException("Invalid password.");
        }

        AppLogger.info("User logged in: " + username + " | Role: " + foundUser.getRole());
        return foundUser;
    }

    public Customer registerCustomer(String username, String password, String name, String phone, String address) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        Customer customer = new Customer(id, username, password, name, phone, address);
        customerRepo.save(customer);
        AppLogger.info("New customer registered: " + username + " | ID: " + id);
        return customer;
    }

    public Admin registerAdmin(String username, String password, String name) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        Admin admin = new Admin(id, username, password, name);
        adminRepo.save(admin);
        AppLogger.info("New admin registered: " + username + " | ID: " + id);
        return admin;
    }

    public DeliveryPartner registerDriver(String username, String password, String name, String phone, String vehicleType) {
        if (isUsernameTaken(username)) {
            throw new DuplicateEntityException("Username already taken: " + username);
        }

        String id = IdGenerator.generateId();
        DeliveryPartner driver = new DeliveryPartner(id, username, password, name, phone, vehicleType);
        driverRepo.save(driver);
        AppLogger.info("New delivery partner registered: " + username + " | ID: " + id);
        return driver;
    }

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

    public boolean isUsernameTaken(String username) {
        return findUserByUsername(username).isPresent();
    }

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
