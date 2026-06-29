package com.tss.FoodApp.service;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;

/**
 * Manages CRUD operations for all user types.
 * SRP: Only handles user data management — auth is in AuthService.
 * Why one UserService for all types? → Toggle active/inactive, view all users — same logic for any type.
 */
public class UserService {

    private final Repository<Admin> adminRepo;
    private final Repository<Customer> customerRepo;
    private final Repository<DeliveryPartner> driverRepo;

    public UserService(Repository<Admin> adminRepo, Repository<Customer> customerRepo,
                       Repository<DeliveryPartner> driverRepo) {
        this.adminRepo = adminRepo;
        this.customerRepo = customerRepo;
        this.driverRepo = driverRepo;
    }

    // --- Customer operations ---

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepo.findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepo.update(customer);
    }

    // --- DeliveryPartner operations ---

    public List<DeliveryPartner> getAllDrivers() {
        return driverRepo.findAll();
    }

    public Optional<DeliveryPartner> getDriverById(String id) {
        return driverRepo.findAll().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
    }

    public DeliveryPartner updateDriver(DeliveryPartner driver) {
        return driverRepo.update(driver);
    }

    // --- Admin operations ---

    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    // --- Toggle active/inactive for any user type ---

    /**
     * Toggle active status of any user by ID.
     * Searches across all repos. Uses Stream API.
     */
    public String toggleUserStatus(String userId) {
        // Try admins
        Optional<Admin> admin = adminRepo.findAll().stream()
                .filter(a -> a.getId().equals(userId)).findFirst();
        if (admin.isPresent()) {
            admin.get().setActive(!admin.get().isActive());
            adminRepo.update(admin.get());
            String status = admin.get().isActive() ? "Active" : "Inactive";
            AppLogger.info("Admin " + admin.get().getUsername() + " set to " + status);
            return admin.get().getName() + " is now " + status;
        }

        // Try customers
        Optional<Customer> customer = customerRepo.findAll().stream()
                .filter(c -> c.getId().equals(userId)).findFirst();
        if (customer.isPresent()) {
            customer.get().setActive(!customer.get().isActive());
            customerRepo.update(customer.get());
            String status = customer.get().isActive() ? "Active" : "Inactive";
            AppLogger.info("Customer " + customer.get().getUsername() + " set to " + status);
            return customer.get().getName() + " is now " + status;
        }

        // Try drivers
        Optional<DeliveryPartner> driver = driverRepo.findAll().stream()
                .filter(d -> d.getId().equals(userId)).findFirst();
        if (driver.isPresent()) {
            driver.get().setActive(!driver.get().isActive());
            driverRepo.update(driver.get());
            String status = driver.get().isActive() ? "Active" : "Inactive";
            AppLogger.info("Driver " + driver.get().getUsername() + " set to " + status);
            return driver.get().getName() + " is now " + status;
        }

        throw new AppException("User not found with ID: " + userId);
    }

    /**
     * Get all users (all types combined) for admin view.
     */
    public List<User> getAllUsers() {
        List<User> all = new ArrayList<>();
        all.addAll(adminRepo.findAll());
        all.addAll(customerRepo.findAll());
        all.addAll(driverRepo.findAll());
        return all;
    }
}