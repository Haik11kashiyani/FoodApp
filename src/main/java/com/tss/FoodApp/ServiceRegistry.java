package com.tss.FoodApp;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.*;
import com.tss.FoodApp.service.*;

public class ServiceRegistry {

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

    // --- Processors ---
    private final OrderProcessor orderProcessor;

    public ServiceRegistry() {
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

        this.orderProcessor = new OrderProcessor(cartService, orderService, discountStrategy);
    }

    // ==================== GETTERS ====================

    public AuthService getAuthService() { return authService; }
    public UserService getUserService() { return userService; }
    public MenuService getMenuService() { return menuService; }
    public CartService getCartService() { return cartService; }
    public OrderService getOrderService() { return orderService; }
    public OrderProcessor getOrderProcessor() { return orderProcessor; }

    public Repository<Admin> getAdminRepo() { return adminRepo; }
    public Repository<Customer> getCustomerRepo() { return customerRepo; }
    public Repository<DeliveryPartner> getDriverRepo() { return driverRepo; }
    public Repository<MenuItem> getMenuRepo() { return menuRepo; }
    public Repository<Order> getOrderRepo() { return orderRepo; }
}
