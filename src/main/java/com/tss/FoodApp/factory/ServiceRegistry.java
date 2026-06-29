package com.tss.FoodApp.factory;

import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.enums.Role;
import com.tss.FoodApp.facade.OrderProcessingFacade;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.FileRepository;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.IdGenerator;

/**
 * SINGLETON PATTERN + FACTORY METHOD
 *
 * SINGLETON: Only ONE instance of ServiceRegistry exists. All services are created once
 * and reused throughout the app. Prevents duplicate repositories (which would cause
 * data inconsistency — two repos loading the same file independently).
 *
 * Why Singleton not static methods?
 * → Singleton holds state (repository/service instances). Static methods can't easily
 *   manage object lifecycle and initialization order.
 * → Alternative: static fields + static initializer block — works but harder to control
 *   when initialization happens. Singleton's getInstance() is explicit.
 *
 * FACTORY METHOD: createUser() creates the right User subtype based on Role.
 * Why not put 'new Admin()' directly in the menu?
 * → If Admin constructor changes (e.g., adds 'department' parameter), you'd fix it in
 *   every place Admin is created. Factory centralizes creation — fix in one place.
 */
public class ServiceRegistry {

    // Singleton instance — volatile for thread safety
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

    /**
     * Private constructor — only called by getInstance().
     * Initializes all repositories and services in correct dependency order.
     */
    private ServiceRegistry() {
        // 1. Create repositories (one per entity type, each with its own file)
        this.adminRepo = new FileRepository<>(AppConfig.ADMIN_FILE);
        this.customerRepo = new FileRepository<>(AppConfig.CUSTOMER_FILE);
        this.driverRepo = new FileRepository<>(AppConfig.DELIVERY_PARTNER_FILE);
        this.menuRepo = new FileRepository<>(AppConfig.MENU_FILE);
        this.orderRepo = new FileRepository<>(AppConfig.ORDER_FILE);

        // 2. Create services (inject repositories via constructor — Dependency Inversion)
        this.authService = new AuthService(adminRepo, customerRepo, driverRepo);
        this.userService = new UserService(adminRepo, customerRepo, driverRepo);
        this.menuService = new MenuService(menuRepo);
        this.cartService = new CartService();
        this.orderService = new OrderService(orderRepo, driverRepo);

        // 3. Create facade (inject services)
        this.orderFacade = new OrderProcessingFacade(cartService, orderService);
    }

    /**
     * Get the singleton instance. Uses double-checked locking for thread safety.
     * Why double-checked locking?
     * → synchronized is slow. We only need it during first creation.
     * → After instance exists, all calls skip the synchronized block — fast.
     * → volatile prevents instruction reordering issues.
     */
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

    // ==================== FACTORY METHOD ====================

    /**
     * Factory Method — Creates the correct User subtype based on Role.
     *
     * Why switch expression (Java 14+) not if-else?
     * → switch expression is exhaustive — compiler warns if a Role is not handled.
     * → if-else can silently miss a case. switch forces you to handle all enum values.
     * → Also: switch expression returns a value directly — no need for 'break' or temp variable.
     */
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

    // Expose repos for async loading
    public Repository<Admin> getAdminRepo() { return adminRepo; }
    public Repository<Customer> getCustomerRepo() { return customerRepo; }
    public Repository<DeliveryPartner> getDriverRepo() { return driverRepo; }
    public Repository<MenuItem> getMenuRepo() { return menuRepo; }
    public Repository<Order> getOrderRepo() { return orderRepo; }
}