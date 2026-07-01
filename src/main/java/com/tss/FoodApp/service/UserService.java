package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.List;
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

    public Customer getCustomerById(String id) {
        return customerRepo.findById(id);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepo.update(customer);
    }

    public List<DeliveryPartner> getAllDrivers() {
        return driverRepo.findAll();
    }

    public DeliveryPartner getDriverById(String id) {
        return driverRepo.findById(id);
    }

    public DeliveryPartner updateDriver(DeliveryPartner driver) {
        return driverRepo.update(driver);
    }

    public List<Admin> getAllAdmins() {
        return adminRepo.findAll();
    }

    public String toggleUserStatus(String userId) {
        Admin admin = adminRepo.findById(userId);
        if (admin != null) {
            admin.setActive(!admin.isActive());
            adminRepo.update(admin);
            String status = admin.isActive() ? "Active" : "Inactive";
            AppLogger.info("User " + admin.getUsername() + " set to " + status);
            return admin.getName() + " is now " + status;
        }

        Customer customer = customerRepo.findById(userId);
        if (customer != null) {
            customer.setActive(!customer.isActive());
            customerRepo.update(customer);
            String status = customer.isActive() ? "Active" : "Inactive";
            AppLogger.info("User " + customer.getUsername() + " set to " + status);
            return customer.getName() + " is now " + status;
        }

        DeliveryPartner driver = driverRepo.findById(userId);
        if (driver != null) {
            driver.setActive(!driver.isActive());
            driverRepo.update(driver);
            String status = driver.isActive() ? "Active" : "Inactive";
            AppLogger.info("User " + driver.getUsername() + " set to " + status);
            return driver.getName() + " is now " + status;
        }

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
