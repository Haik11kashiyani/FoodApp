package com.tss.FoodApp;

import java.util.*;
import java.util.stream.Collectors;

public class Services {
    private Services() {} // Container class
}

// ==================== DISCOUNT STRATEGY (OCP) ====================
// To add a new discount type (coupon, loyalty, buy-one-get-one):
//   1. Create: class CouponDiscount implements DiscountStrategy { ... }
//   2. Set it: orderFacade.setDiscountStrategy(new CouponDiscount(...));
//   Zero changes to OrderProcessingFacade!

interface DiscountStrategy {
    double calculateDiscount(double totalAmount);
    String getDescription();
}

class PercentageDiscount implements DiscountStrategy {
    private double percentage;
    private double threshold;

    public PercentageDiscount(double percentage, double threshold) {
        this.percentage = percentage;
        this.threshold = threshold;
    }

    @Override
    public double calculateDiscount(double totalAmount) {
        return (totalAmount >= threshold) ? totalAmount * (percentage / 100.0) : 0;
    }

    @Override
    public String getDescription() {
        return String.format("%.0f%% off on orders above Rs. %.0f", percentage, threshold);
    }

    public double getPercentage() { return percentage; }
    public double getThreshold() { return threshold; }
}

// ==================== ORDER EVENT LISTENER (OCP) ====================
// To add a new behavior on order status change (email notification, analytics):
//   1. Create: class EmailNotifier implements OrderEventListener { ... }
//   2. Register in ServiceRegistry: orderListeners.add(new EmailNotifier());
//   Zero changes to OrderService!

interface OrderEventListener {
    void onStatusChanged(Order order, OrderStatus newStatus);
}

class DriverAvailabilityListener implements OrderEventListener {
    private final Repository<DeliveryPartner> driverRepo;

    public DriverAvailabilityListener(Repository<DeliveryPartner> driverRepo) {
        this.driverRepo = driverRepo;
    }

    @Override
    public void onStatusChanged(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.DELIVERED) {
            driverRepo.findById(order.getDeliveryPartnerId())
                .ifPresent(driver -> {
                    driver.setAvailable(true);
                    driverRepo.update(driver);
                    AppLogger.info("Driver " + driver.getName() + " marked available after delivery");
                });
        }
    }
}

// ==================== AUTH SERVICE ====================

class AuthService {
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

// ==================== USER SERVICE ====================

class UserService {
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

// ==================== MENU SERVICE ====================

class MenuService {
    private final Repository<MenuItem> menuRepo;

    public MenuService(Repository<MenuItem> menuRepo) {
        this.menuRepo = menuRepo;
    }

    public MenuItem addItem(String name, double price, FoodCategory category, CuisineType cuisineType) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Menu item name cannot be empty.");
        }
        if (price <= 0) {
            throw new ValidationException("Price must be greater than zero.");
        }

        String id = IdGenerator.generateId();
        MenuItem item = new MenuItem(id, name, price, category, cuisineType);
        menuRepo.save(item);
        AppLogger.info("Menu item added: " + name + " | Rs. " + price + " | ID: " + id);
        return item;
    }

    public MenuItem updateItem(String id, String newName, double newPrice, FoodCategory newCategory, CuisineType newCuisineType) {
        MenuItem item = menuRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem", id));

        item.setName(newName);
        item.setPrice(newPrice);
        item.setCategory(newCategory);
        item.setCuisineType(newCuisineType);
        menuRepo.update(item);
        AppLogger.info("Menu item updated: " + newName + " | ID: " + id);
        return item;
    }

    public boolean deleteItem(String itemId) {
        boolean deleted = menuRepo.deleteById(itemId);
        if (!deleted) {
            throw new EntityNotFoundException("MenuItem", itemId);
        }
        AppLogger.info("Menu item deleted | ID: " + itemId);
        return true;
    }

    public List<MenuItem> getAllItems() {
        return menuRepo.findAll();
    }

    public List<MenuItem> getAvailableItems() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    public List<MenuItem> searchByName(String keyword) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    public List<MenuItem> filterByCategory(FoodCategory category) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<MenuItem> filterByCuisineAndCategory(CuisineType cuisine, FoodCategory category) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getCuisineType() == cuisine && item.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<MenuItem> sortByPriceAsc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice))
                .collect(Collectors.toList());
    }

    public List<MenuItem> sortByPriceDesc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice).reversed())
                .collect(Collectors.toList());
    }

    public List<MenuItem> sortByName() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }

    public MenuItem getItemById(String itemId) {
        return menuRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem", itemId));
    }
}

// ==================== CART SERVICE ====================

class CartService {
    private final Map<String, List<CartItem>> carts = new HashMap<>();

    public void addToCart(String customerId, MenuItem menuItem, int quantity) {
        List<CartItem> cart = carts.computeIfAbsent(customerId, k -> new ArrayList<>());

        Optional<CartItem> existing = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            AppLogger.info("Cart updated — increased quantity for: " + menuItem.getName());
        } else {
            cart.add(new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
            AppLogger.info("Item added to cart: " + menuItem.getName() + " x" + quantity);
        }
    }

    public void removeFromCart(String customerId, String menuItemId) {
        List<CartItem> cart = getCart(customerId);
        boolean removed = cart.removeIf(ci -> ci.getMenuItemId().equals(menuItemId));
        if (!removed) {
            throw new EntityNotFoundException("CartItem", menuItemId);
        }
        AppLogger.info("Item removed from cart | Customer: " + customerId);
    }

    public void updateQuantity(String customerId, String menuItemId, int newQuantity) {
        List<CartItem> cart = getCart(customerId);
        CartItem item = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("CartItem", menuItemId));

        item.setQuantity(newQuantity);
        AppLogger.info("Cart quantity updated: " + item.getItemName() + " -> " + newQuantity);
    }

    public List<CartItem> getCart(String customerId) {
        return carts.getOrDefault(customerId, new ArrayList<>());
    }

    public double getCartTotal(String customerId) {
        return getCart(customerId).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public boolean isCartEmpty(String customerId) {
        return getCart(customerId).isEmpty();
    }

    public void clearCart(String customerId) {
        carts.remove(customerId);
        AppLogger.info("Cart cleared for customer: " + customerId);
    }
}

// ==================== ORDER SERVICE ====================

class OrderService {
    private final Repository<Order> orderRepo;
    private final Repository<DeliveryPartner> driverRepo;
    private final List<OrderEventListener> listeners;
    private final Random random = new Random();

    public OrderService(Repository<Order> orderRepo, Repository<DeliveryPartner> driverRepo,
                        List<OrderEventListener> listeners) {
        this.orderRepo = orderRepo;
        this.driverRepo = driverRepo;
        this.listeners = listeners;
    }

    public Order createOrder(String customerId, String customerName, List<CartItem> items,
                             double totalAmount, double discountAmount, double finalAmount,
                             PaymentMode paymentMode, String driverId, String driverName) {
        String id = IdGenerator.generateId();
        Order order = new Order(id, customerId, customerName, items,
                totalAmount, discountAmount, finalAmount, paymentMode, driverId, driverName);
        orderRepo.save(order);
        AppLogger.info("Order created: " + id + " | Customer: " + customerName + " | Total: Rs. " + finalAmount);
        return order;
    }

    public DeliveryPartner assignRandomDriver() {
        List<DeliveryPartner> availableDrivers = driverRepo.findAll().stream()
                .filter(d -> d.isActive() && d.isAvailable())
                .collect(Collectors.toList());

        if (availableDrivers.isEmpty()) {
            throw new AppException("No delivery partners available at the moment. Please try again later.");
        }

        DeliveryPartner selected = availableDrivers.get(random.nextInt(availableDrivers.size()));
        AppLogger.info("Delivery partner assigned: " + selected.getName() + " | ID: " + selected.getId());
        return selected;
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        // O(1) lookup using ConcurrentHashMap cache
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        // Validate transition (SRP — OrderStatus enum owns its own rules)
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new ValidationException(
                    "Cannot change status from " + order.getStatus() + " to " + newStatus
                    + ". Next valid status: " + getNextValidStatus(order.getStatus()));
        }

        order.setStatus(newStatus);
        orderRepo.update(order);

        // Notify all listeners (OCP — add new behaviors without editing this method)
        for (OrderEventListener listener : listeners) {
            listener.onStatusChanged(order, newStatus);
        }

        AppLogger.info("Order " + orderId + " status updated to: " + newStatus);
        return order;
    }

    private String getNextValidStatus(OrderStatus current) {
        switch (current) {
            case PLACED: return "PREPARING or CANCELLED";
            case PREPARING: return "OUT_FOR_DELIVERY or CANCELLED";
            case OUT_FOR_DELIVERY: return "DELIVERED or CANCELLED";
            default: return "None (terminal status)";
        }
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByDriver(String driverId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getDeliveryPartnerId().equals(driverId))
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Order> getActiveOrdersForDriver(String driverId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getDeliveryPartnerId().equals(driverId))
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())
                .collect(Collectors.toList());
    }

    public void printInvoice(Order order) {
        String line = InputUtil.repeat("=", 50);
        System.out.println("\n+" + line + "+");
        System.out.println("|" + centerText("INVOICE", 50) + "|");
        System.out.println("+" + line + "+");
        System.out.printf("| Order ID    : %-35s |%n", order.getId());
        System.out.printf("| Customer    : %-35s |%n", order.getCustomerName());
        System.out.printf("| Date        : %-35s |%n", order.getOrderedAt());
        System.out.println("+" + line + "+");
        System.out.println("|" + centerText("ITEMS", 50) + "|");
        System.out.println("+" + line + "+");

        for (CartItem item : order.getItems()) {
            String itemLine = String.format("  %-18s x%-3d  Rs. %8.2f", item.getItemName(), item.getQuantity(), item.getSubtotal());
            System.out.printf("| %-48s |%n", itemLine);
        }

        System.out.println("+" + line + "+");
        System.out.printf("| Subtotal    : Rs. %-34.2f |%n", order.getTotalAmount());
        System.out.printf("| Discount    : -Rs. %-33.2f |%n", order.getDiscountAmount());
        System.out.printf("| %-48s |%n", InputUtil.repeat("-", 48));
        System.out.printf("| TOTAL       : Rs. %-34.2f |%n", order.getFinalAmount());
        System.out.println("+" + line + "+");
        System.out.printf("| Payment     : %-35s |%n", order.getPaymentMode());
        System.out.printf("| Delivery By : %-35s |%n", order.getDeliveryPartnerName());
        System.out.printf("| Status      : %-35s |%n", order.getStatus());
        System.out.println("+" + line + "+");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return InputUtil.repeat(" ", Math.max(0, padding)) + text + InputUtil.repeat(" ", Math.max(0, width - padding - text.length()));
    }
}

// ==================== ORDER PROCESSING FACADE ====================

class OrderProcessingFacade {
    private final CartService cartService;
    private final OrderService orderService;
    private final Map<PaymentMode, IPaymentStrategy> paymentStrategies;
    private DiscountStrategy discountStrategy;

    public OrderProcessingFacade(CartService cartService, OrderService orderService,
                                 Map<PaymentMode, IPaymentStrategy> paymentStrategies,
                                 DiscountStrategy discountStrategy) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentStrategies = paymentStrategies;
        this.discountStrategy = discountStrategy;
    }

    public Order placeOrder(String customerId, String customerName, PaymentMode paymentMode) {
        if (cartService.isCartEmpty(customerId)) {
            throw new ValidationException("Cart is empty! Add items before placing an order.");
        }

        List<CartItem> items = new ArrayList<>(cartService.getCart(customerId));
        double totalAmount = cartService.getCartTotal(customerId);

        // Apply discount (OCP — swap strategy without editing this method)
        double discountAmount = discountStrategy.calculateDiscount(totalAmount);
        if (discountAmount > 0) {
            System.out.println("\n  " + discountStrategy.getDescription());
            System.out.printf("   You save: Rs. %.2f%n", discountAmount);
        }
        double finalAmount = totalAmount - discountAmount;

        // Process payment (OCP — add new payment modes without editing this method)
        IPaymentStrategy paymentStrategy = paymentStrategies.get(paymentMode);
        if (paymentStrategy == null) {
            throw new PaymentException("Unsupported payment mode: " + paymentMode);
        }
        paymentStrategy.processPayment(finalAmount);

        // Assign driver and create order
        DeliveryPartner driver = orderService.assignRandomDriver();
        Order order = orderService.createOrder(customerId, customerName, items,
                totalAmount, discountAmount, finalAmount, paymentMode,
                driver.getId(), driver.getName());

        orderService.printInvoice(order);
        cartService.clearCart(customerId);

        AppLogger.info("Order placed successfully via Facade | Order ID: " + order.getId());
        return order;
    }

    public DiscountStrategy getDiscountStrategy() { return discountStrategy; }

    public void setDiscountStrategy(DiscountStrategy strategy) {
        this.discountStrategy = strategy;
        AppLogger.info("Discount strategy updated: " + strategy.getDescription());
    }
}
