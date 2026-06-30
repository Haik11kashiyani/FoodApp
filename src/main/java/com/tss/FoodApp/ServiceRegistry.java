package com.tss.FoodApp;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.*;

public class ServiceRegistry {

    private static ServiceRegistry instance;

    // --- Repositories ---
    private final Repository<Admin> adminRepo;
    private final Repository<Customer> customerRepo;
    private final Repository<DeliveryPartner> driverRepo;
    private final Repository<MenuItem> menuRepo;
    private final Repository<Order> orderRepo;

    // --- Services ---
    private final AuthService authService;
    private final UserService userService;
    private final MenuService menuService;
    private final CartService cartService;
    private final OrderService orderService;

    // --- Facade ---
    private final OrderProcessingFacade orderFacade;

    private ServiceRegistry() {
        this.adminRepo = new FileRepository<>(AppConfig.ADMIN_FILE);
        this.customerRepo = new FileRepository<>(AppConfig.CUSTOMER_FILE);
        this.driverRepo = new FileRepository<>(AppConfig.DELIVERY_PARTNER_FILE);
        this.menuRepo = new FileRepository<>(AppConfig.MENU_FILE);
        this.orderRepo = new FileRepository<>(AppConfig.ORDER_FILE);

        this.authService = new AuthService(adminRepo, customerRepo, driverRepo);
        this.userService = new UserService(adminRepo, customerRepo, driverRepo);
        this.menuService = new MenuService(menuRepo);
        this.cartService = new CartService();
        this.orderService = new OrderService(orderRepo, driverRepo);

        // Default discount strategy
        PercentageDiscount discountStrategy = new PercentageDiscount(
                AppConfig.DEFAULT_DISCOUNT_PERCENTAGE,
                AppConfig.DEFAULT_DISCOUNT_THRESHOLD
        );

        this.orderFacade = new OrderProcessingFacade(cartService, orderService, discountStrategy);
    }

    public static ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    public static User createUser(Role role, String username, String password, String name,
                                  String phone, String address, String vehicleType) {
        String id = IdGenerator.generateId();
        switch (role) {
            case ADMIN:
                return new Admin(id, username, password, name);
            case CUSTOMER:
                return new Customer(id, username, password, name, phone, address);
            case DELIVERY_PARTNER:
                return new DeliveryPartner(id, username, password, name, phone, vehicleType);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    // ==================== GETTERS ====================

    public AuthService getAuthService() { return authService; }
    public UserService getUserService() { return userService; }
    public MenuService getMenuService() { return menuService; }
    public CartService getCartService() { return cartService; }
    public OrderService getOrderService() { return orderService; }
    public OrderProcessingFacade getOrderFacade() { return orderFacade; }

    public Repository<Admin> getAdminRepo() { return adminRepo; }
    public Repository<Customer> getCustomerRepo() { return customerRepo; }
    public Repository<DeliveryPartner> getDriverRepo() { return driverRepo; }
    public Repository<MenuItem> getMenuRepo() { return menuRepo; }
    public Repository<Order> getOrderRepo() { return orderRepo; }
}
