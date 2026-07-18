package com.tss.FoodApp;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.*;
import com.tss.FoodApp.repository.jdbc.*;
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
        this.adminRepo = new AdminJdbcRepository();
        this.customerRepo = new CustomerJdbcRepository();
        this.driverRepo = new DeliveryPartnerJdbcRepository();
        this.menuRepo = new MenuItemJdbcRepository();
        this.orderRepo = new OrderJdbcRepository();

        this.authService = new AuthService(adminRepo, customerRepo, driverRepo);
        this.userService = new UserService(adminRepo, customerRepo, driverRepo);
        this.menuService = new MenuService(menuRepo);
        Repository<com.tss.FoodApp.model.Cart> cartRepo = new CartJdbcRepository();
        this.cartService = new CartService(cartRepo);
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

}
