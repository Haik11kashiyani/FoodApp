package com.tss.FoodApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.ui.*;
import com.tss.FoodApp.util.*;

/**
 * SINGLETON PATTERN + FACTORY METHOD
 */
public class ServiceRegistry {

    private static volatile ServiceRegistry instance;

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

    // --- SOLID registries ---
    private final Map<Role, DashboardFactory> dashboardFactories;

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

        // Wire Order Event Listeners (OCP)
        List<OrderEventListener> listeners = new ArrayList<>();
        listeners.add(new DriverAvailabilityListener(driverRepo));

        this.orderService = new OrderService(orderRepo, driverRepo, listeners);

        // Inject payment strategies map
        Map<PaymentMode, IPaymentStrategy> paymentStrategies = new HashMap<>();
        paymentStrategies.put(PaymentMode.CASH, new CashPayment());
        paymentStrategies.put(PaymentMode.UPI, new UpiPayment());

        // Default discount strategy (OCP)
        DiscountStrategy discountStrategy = new PercentageDiscount(
                AppConfig.DEFAULT_DISCOUNT_PERCENTAGE,
                AppConfig.DEFAULT_DISCOUNT_THRESHOLD
        );

        this.orderFacade = new OrderProcessingFacade(cartService, orderService, paymentStrategies, discountStrategy);

        // Wire Role Dashboard Factories (OCP + DIP)
        this.dashboardFactories = new HashMap<>();
        dashboardFactories.put(Role.ADMIN, (user, reg) -> new AdminMenu(user, reg).show());
        dashboardFactories.put(Role.CUSTOMER, (user, reg) -> new CustomerMenu(user, reg).show());
        dashboardFactories.put(Role.DELIVERY_PARTNER, (user, reg) -> new DeliveryPartnerMenu(user, reg).show());
    }

    public static ServiceRegistry getInstance() {
        if (instance == null) {
            synchronized (ServiceRegistry.class) {
                if (instance == null) {
                    instance = new ServiceRegistry();
                }
            }
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

    public DashboardFactory getDashboardFactory(Role role) {
        return dashboardFactories.get(role);
    }
}
