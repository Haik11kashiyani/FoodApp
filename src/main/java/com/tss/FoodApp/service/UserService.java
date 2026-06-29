package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.util.AppLogger;

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

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepo.findById(id);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepo.update(customer);
    }

    public List<DeliveryPartner> getAllDrivers() {
        return driverRepo.findAll();
    }

    public Optional<DeliveryPartner> getDriverById(String id) {
        return driverRepo.findById(id);
    }

    public DeliveryPartner updateDriver(DeliveryPartner driver) {
        return driverRepo.update(driver);
    }

    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    private <T extends User> String tryToggle(Repository<T> repo, String userId) {
        Optional<T> found = repo.findById(userId);
        if (found.isPresent()) {
            T user = found.get();
            user.setActive(!user.isActive());
            repo.update(user);
            String status = user.isActive() ? "Active" : "Inactive";
            AppLogger.info("User " + user.getUsername() + " set to " + status);
            return user.getName() + " is now " + status;
        }
        return null;
    }

    public String toggleUserStatus(String userId) {
        String result = tryToggle(adminRepo, userId);
        if (result != null) return result;

        result = tryToggle(customerRepo, userId);
        if (result != null) return result;

        result = tryToggle(driverRepo, userId);
        if (result != null) return result;

        throw new EntityNotFoundException("User", userId);
    }

    public List<User> getAllUsers() {
        List<User> all = new ArrayList<>();
        all.addAll(adminRepo.findAll());
        all.addAll(customerRepo.findAll());
        all.addAll(driverRepo.findAll());
        return all;
    }
}
