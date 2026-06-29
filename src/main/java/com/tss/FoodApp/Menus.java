package com.tss.FoodApp;

import java.util.ArrayList;
import java.util.List;

public class Menus {
    private Menus() {} // Container class
}

// ==================== PLUGGABLE MENU COMMAND (OCP) ====================

interface MenuCommand {
    String getLabel();
    void execute();
}

abstract class AbstractMenuCommand implements MenuCommand {
    private final String label;

    public AbstractMenuCommand(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}

// ==================== DASHBOARD FACTORY (OCP + DIP) ====================
// To add a new user role dashboard:
//   1. Create: class RestaurantMenu { ... }
//   2. Register in ServiceRegistry: dashboardFactories.put(Role.RESTAURANT, ...);
//   Zero changes to MainApp!

interface DashboardFactory {
    void showDashboard(User user, ServiceRegistry registry);
}

// ==================== ADMIN DASHBOARD ====================

class AdminMenu {
    private final User admin;
    private final MenuService menuService;
    private final UserService userService;
    private final AuthService authService;
    private final OrderService orderService;
    private final OrderProcessingFacade orderFacade;
    private final List<MenuCommand> commands = new ArrayList<>();

    public AdminMenu(User admin, ServiceRegistry registry) {
        this.admin = admin;
        this.menuService = registry.getMenuService();
        this.userService = registry.getUserService();
        this.authService = registry.getAuthService();
        this.orderService = registry.getOrderService();
        this.orderFacade = registry.getOrderFacade();

        // Register pluggable commands (OCP)
        commands.add(new AbstractMenuCommand("Menu Management") {
            @Override public void execute() { showMenuManagement(); }
        });
        commands.add(new AbstractMenuCommand("Discount Settings") {
            @Override public void execute() { showDiscountSettings(); }
        });
        commands.add(new AbstractMenuCommand("User Management") {
            @Override public void execute() { showUserManagement(); }
        });
        commands.add(new AbstractMenuCommand("Orders Management") {
            @Override public void execute() { showOrdersManagement(); }
        });
    }

    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("ADMIN DASHBOARD");
            System.out.println("  Welcome, " + admin.getName() + "!");
            InputUtil.printDivider();

            for (int i = 0; i < commands.size(); i++) {
                InputUtil.printMenuOption(i + 1, commands.get(i).getLabel());
            }
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(commands.size() + 1, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, commands.size() + 1);

            if (choice == commands.size() + 1) {
                InputUtil.printSuccess("Logged out successfully.");
                running = false;
            } else {
                try {
                    commands.get(choice - 1).execute();
                } catch (AppException e) {
                    InputUtil.printError(e.getMessage());
                }
            }
        }
    }

    private void showMenuManagement() {
        InputUtil.runSubMenu("MENU MANAGEMENT",
            new String[]{"Add Menu Item", "Update Menu Item", "Delete Menu Item", "View All Menu Items"},
            new Runnable[]{this::addMenuItem, this::updateMenuItem, this::deleteMenuItem, this::viewAllMenuItems});
    }

    private void showDiscountSettings() {
        InputUtil.runSubMenu("DISCOUNT SETTINGS",
            new String[]{"View Discount Settings", "Update Discount Settings"},
            new Runnable[]{this::viewDiscountSettings, this::updateDiscountSettings});
    }

    private void showUserManagement() {
        InputUtil.runSubMenu("USER MANAGEMENT",
            new String[]{"Add New Admin", "Add Delivery Partner", "View All Customers", "View All Delivery Partners", "View All Admins", "Toggle User Active/Inactive"},
            new Runnable[]{this::addNewAdmin, this::addDeliveryPartner, this::viewAllCustomers, this::viewAllDrivers, this::viewAllAdmins, this::toggleUserStatus});
    }

    private void showOrdersManagement() {
        InputUtil.runSubMenu("ORDERS MANAGEMENT",
            new String[]{"View All Orders"},
            new Runnable[]{this::viewAllOrders});
    }

    private void addMenuItem() {
        InputUtil.printHeader("ADD MENU ITEM");
        String name = InputUtil.readString("  Item name: ");
        double price = InputUtil.readDouble("  Price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readEnum("  Cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("  Category", FoodCategory.class);

        MenuItem item = menuService.addItem(name, price, category, cuisineType);
        InputUtil.printSuccess("Menu item added: " + item.getName() + " | ID: " + item.getId());
    }

    private void updateMenuItem() {
        InputUtil.printHeader("UPDATE MENU ITEM");
        viewAllMenuItems();
        String itemId = InputUtil.readString("  Enter item ID to update: ");

        String name = InputUtil.readString("  New name: ");
        double price = InputUtil.readDouble("  New price: ", 1, AppConfig.MAX_PRICE);
        CuisineType cuisineType = InputUtil.readEnum("  New cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("  New category", FoodCategory.class);

        MenuItem updated = menuService.updateItem(itemId, name, price, category, cuisineType);
        InputUtil.printSuccess("Updated: " + updated.getName());
    }

    private void deleteMenuItem() {
        InputUtil.printHeader("DELETE MENU ITEM");
        viewAllMenuItems();
        String itemId = InputUtil.readString("  Enter item ID to delete: ");

        if (InputUtil.readYesNo("  Are you sure?")) {
            menuService.deleteItem(itemId);
            InputUtil.printSuccess("Menu item deleted.");
        }
    }

    private void viewAllMenuItems() {
        List<MenuItem> items = menuService.getAllItems();
        InputUtil.printNumberedList(items, "MENU ITEMS");
    }

    private void viewDiscountSettings() {
        InputUtil.printHeader("DISCOUNT SETTINGS");
        DiscountStrategy strategy = orderFacade.getDiscountStrategy();
        System.out.println("  Current Strategy: " + strategy.getDescription());
        System.out.println("  Rule: Orders above the minimum amount get the discount automatically.");
    }

    private void updateDiscountSettings() {
        viewDiscountSettings();
        double percentage = InputUtil.readDouble("  New discount percentage (1-50): ", 1, 50);
        double threshold = InputUtil.readDouble("  New minimum order amount: ", 0, 100000);
        orderFacade.setDiscountStrategy(new PercentageDiscount(percentage, threshold));
        InputUtil.printSuccess("Discount settings updated!");
    }

    private void addNewAdmin() {
        InputUtil.printHeader("ADD NEW ADMIN");
        String username = InputUtil.readValidUsername("  Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("  Password: ");
        if (password == null) return;
        String name = InputUtil.readString("  Full name: ");

        Admin newAdmin = authService.registerAdmin(username, password, name);
        InputUtil.printSuccess("Admin created: " + newAdmin.getName() + " | ID: " + newAdmin.getId());
    }

    private void addDeliveryPartner() {
        InputUtil.printHeader("ADD DELIVERY PARTNER");
        String username = InputUtil.readValidUsername("  Username: ");
        if (username == null) return;
        String password = InputUtil.readValidPassword("  Password: ");
        if (password == null) return;
        String name = InputUtil.readString("  Full name: ");
        String phone = InputUtil.readValidPhone("  Phone: ");
        if (phone == null) return;
        String vehicleType = InputUtil.readString("  Vehicle type (Bike/Bicycle/Car): ");

        DeliveryPartner driver = authService.registerDriver(username, password, name, phone, vehicleType);
        InputUtil.printSuccess("Delivery partner created: " + driver.getName() + " | ID: " + driver.getId());
    }

    private void viewAllCustomers() {
        List<Customer> customers = userService.getAllCustomers();
        InputUtil.printNumberedList(customers, "CUSTOMERS");
    }

    private void viewAllDrivers() {
        List<DeliveryPartner> drivers = userService.getAllDrivers();
        InputUtil.printNumberedList(drivers, "DELIVERY PARTNERS");
    }

    private void viewAllAdmins() {
        List<Admin> admins = userService.getAllAdmins();
        InputUtil.printNumberedList(admins, "ADMINS");
    }

    private void toggleUserStatus() {
        InputUtil.printHeader("TOGGLE USER STATUS");
        List<User> allUsers = userService.getAllUsers();
        InputUtil.printNumberedList(allUsers, "ALL USERS");
        String userId = InputUtil.readString("  Enter user ID to toggle: ");
        String result = userService.toggleUserStatus(userId);
        InputUtil.printSuccess(result);
    }

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            InputUtil.printWarning("No orders found.");
            return;
        }
        InputUtil.printNumberedList(orders, "ALL ORDERS");

        if (InputUtil.readYesNo("  View order details?")) {
            String orderId = InputUtil.readString("  Enter order ID: ");
            java.util.Optional<Order> orderOpt = orders.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst();
            if (orderOpt.isPresent()) {
                orderService.printInvoice(orderOpt.get());
            } else {
                InputUtil.printError("Order not found.");
            }
        }
    }
}

// ==================== CUSTOMER DASHBOARD ====================

class CustomerMenu {
    private final Customer customer;
    private final MenuService menuService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderProcessingFacade orderFacade;
    private final List<MenuCommand> commands = new ArrayList<>();

    public CustomerMenu(User user, ServiceRegistry registry) {
        this.customer = (Customer) user;
        this.menuService = registry.getMenuService();
        this.cartService = registry.getCartService();
        this.orderService = registry.getOrderService();
        this.orderFacade = registry.getOrderFacade();

        // Register pluggable commands (OCP)
        commands.add(new AbstractMenuCommand("Menu") {
            @Override public void execute() { showMenuBrowsing(); }
        });
        commands.add(new AbstractMenuCommand("Cart") {
            @Override public void execute() { showCartOperations(); }
        });
        commands.add(new AbstractMenuCommand("Orders") {
            @Override public void execute() { showOrders(); }
        });
        commands.add(new AbstractMenuCommand("Account") {
            @Override public void execute() { showAccount(); }
        });
    }

    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("CUSTOMER DASHBOARD");
            System.out.println("  Welcome, " + customer.getName() + "!");
            InputUtil.printDivider();

            for (int i = 0; i < commands.size(); i++) {
                InputUtil.printMenuOption(i + 1, commands.get(i).getLabel());
            }
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(commands.size() + 1, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, commands.size() + 1);

            if (choice == commands.size() + 1) {
                InputUtil.printSuccess("Logged out successfully.");
                running = false;
            } else {
                try {
                    commands.get(choice - 1).execute();
                } catch (AppException e) {
                    InputUtil.printError(e.getMessage());
                }
            }
        }
    }

    private void showMenuBrowsing() {
        InputUtil.runSubMenu("MENU",
            new String[]{"View Menu", "Search Menu by Name", "Filter Menu by Category", "Sort Menu by Price"},
            new Runnable[]{this::viewMenu, this::searchMenu, this::filterByCategory, this::sortByPrice});
    }

    private void showCartOperations() {
        InputUtil.runSubMenu("CART",
            new String[]{"Add to Cart", "View Cart", "Remove from Cart", "Update Item Quantity", "Clear Cart"},
            new Runnable[]{this::addToCart, this::viewCart, this::removeFromCart, this::updateCartQuantity, this::clearCart});
    }

    private void showOrders() {
        InputUtil.runSubMenu("ORDERS",
            new String[]{"Place Order", "View Order History"},
            new Runnable[]{this::placeOrder, this::viewOrderHistory});
    }

    private void showAccount() {
        InputUtil.runSubMenu("ACCOUNT",
            new String[]{"Update Profile"},
            new Runnable[]{this::updateProfile});
    }

    private void viewMenu() {
        CuisineType cuisine = InputUtil.readEnum("  Select Cuisine", CuisineType.class);
        FoodCategory category = InputUtil.readEnum("  Select Category", FoodCategory.class);
        List<MenuItem> items = menuService.filterByCuisineAndCategory(cuisine, category);
        InputUtil.printNumberedList(items, cuisine.name() + " " + category.name() + " MENU");
    }

    private void searchMenu() {
        String keyword = InputUtil.readString("  Search for: ");
        List<MenuItem> results = menuService.searchByName(keyword);
        InputUtil.printNumberedList(results, "SEARCH RESULTS for '" + keyword + "'");
    }

    private void filterByCategory() {
        FoodCategory category = InputUtil.readEnum("  Filter by category", FoodCategory.class);
        List<MenuItem> results = menuService.filterByCategory(category);
        InputUtil.printNumberedList(results, category.name() + " ITEMS");
    }

    private void sortByPrice() {
        System.out.println("  1. Low to High");
        System.out.println("  2. High to Low");
        int sortChoice = InputUtil.readInt("  Sort order: ", 1, 2);
        List<MenuItem> sorted = (sortChoice == 1) ?
                menuService.sortByPriceAsc() : menuService.sortByPriceDesc();
        InputUtil.printNumberedList(sorted, "MENU (SORTED BY PRICE)");
    }

    private void addToCart() {
        viewMenu();
        List<MenuItem> available = menuService.getAvailableItems();
        if (available.isEmpty()) {
            InputUtil.printWarning("No menu items available.");
            return;
        }

        String itemId = InputUtil.readString("  Enter item ID to add: ");
        MenuItem item = menuService.getItemById(itemId);

        if (!item.isAvailable()) {
            InputUtil.printError("This item is currently unavailable.");
            return;
        }

        int quantity = InputUtil.readInt("  Quantity (1-50): ", 1, AppConfig.MAX_QUANTITY);
        cartService.addToCart(customer.getId(), item, quantity);
        InputUtil.printSuccess(item.getName() + " x" + quantity + " added to cart!");
    }

    private void viewCart() {
        List<CartItem> cart = cartService.getCart(customer.getId());
        if (cart.isEmpty()) {
            InputUtil.printWarning("Your cart is empty.");
            return;
        }
        InputUtil.printNumberedList(cart, "YOUR CART");
        System.out.printf("  Cart Total: Rs. %.2f%n", cartService.getCartTotal(customer.getId()));
    }

    private void removeFromCart() {
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        String itemId = InputUtil.readString("  Enter item ID to remove: ");
        cartService.removeFromCart(customer.getId(), itemId);
        InputUtil.printSuccess("Item removed from cart.");
    }

    private void updateCartQuantity() {
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        String itemId = InputUtil.readString("  Enter item ID to update: ");
        int newQty = InputUtil.readInt("  New quantity (1-50): ", 1, AppConfig.MAX_QUANTITY);
        cartService.updateQuantity(customer.getId(), itemId, newQty);
        InputUtil.printSuccess("Quantity updated.");
    }

    private void clearCart() {
        if (cartService.isCartEmpty(customer.getId())) {
            InputUtil.printWarning("Cart is already empty.");
            return;
        }
        if (InputUtil.readYesNo("  Clear entire cart?")) {
            cartService.clearCart(customer.getId());
            InputUtil.printSuccess("Cart cleared.");
        }
    }

    private void placeOrder() {
        InputUtil.printHeader("PLACE ORDER");
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        double cartTotal = cartService.getCartTotal(customer.getId());
        System.out.printf("%n  Cart Total: Rs. %.2f%n", cartTotal);

        DiscountStrategy strategy = orderFacade.getDiscountStrategy();
        if (strategy.calculateDiscount(cartTotal) > 0) {
            System.out.println("  You're eligible! " + strategy.getDescription());
        }

        PaymentMode mode = InputUtil.readEnum("\n  Select payment method", PaymentMode.class);

        if (InputUtil.readYesNo("  Confirm order?")) {
            Order order = orderFacade.placeOrder(customer.getId(), customer.getName(), mode);
            InputUtil.printSuccess("Order placed successfully! Order ID: " + order.getId());
        }
    }

    private void viewOrderHistory() {
        List<Order> orders = orderService.getOrdersByCustomer(customer.getId());
        if (orders.isEmpty()) {
            InputUtil.printWarning("No orders found.");
            return;
        }
        InputUtil.printNumberedList(orders, "YOUR ORDERS");

        if (InputUtil.readYesNo("  View order details?")) {
            String orderId = InputUtil.readString("  Enter order ID: ");
            java.util.Optional<Order> orderOpt = orders.stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst();
            if (orderOpt.isPresent()) {
                orderService.printInvoice(orderOpt.get());
            } else {
                InputUtil.printError("Order not found.");
            }
        }
    }

    private void updateProfile() {
        InputUtil.printHeader("UPDATE PROFILE");
        System.out.println("  Current Name    : " + customer.getName());
        System.out.println("  Current Phone   : " + customer.getPhone());
        System.out.println("  Current Address : " + customer.getAddress());
        InputUtil.printDivider();

        if (InputUtil.readYesNo("  Update name?")) {
            customer.setName(InputUtil.readString("  New name: "));
        }
        if (InputUtil.readYesNo("  Update phone?")) {
            String phone = InputUtil.readValidPhone("  New phone: ");
            if (phone != null) {
                customer.setPhone(phone);
            }
        }
        if (InputUtil.readYesNo("  Update address?")) {
            customer.setAddress(InputUtil.readLine("  New address: "));
        }

        ServiceRegistry.getInstance().getUserService().updateCustomer(customer);
        InputUtil.printSuccess("Profile updated!");
    }
}

// ==================== DELIVERY PARTNER DASHBOARD ====================

class DeliveryPartnerMenu {
    private final DeliveryPartner driver;
    private final OrderService orderService;
    private final UserService userService;
    private final List<MenuCommand> commands = new ArrayList<>();

    public DeliveryPartnerMenu(User user, ServiceRegistry registry) {
        this.driver = (DeliveryPartner) user;
        this.orderService = registry.getOrderService();
        this.userService = registry.getUserService();

        // Register pluggable commands (OCP)
        commands.add(new AbstractMenuCommand("View Assigned Orders") {
            @Override public void execute() { viewAssignedOrders(); }
        });
        commands.add(new AbstractMenuCommand("Update Order Status") {
            @Override public void execute() { updateOrderStatus(); }
        });
        commands.add(new AbstractMenuCommand("View Delivery History") {
            @Override public void execute() { viewDeliveryHistory(); }
        });
        commands.add(new AbstractMenuCommand("Toggle My Availability") {
            @Override public void execute() { toggleAvailability(); }
        });
    }

    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("DELIVERY PARTNER DASHBOARD");
            System.out.println("  Welcome, " + driver.getName() + "!");
            System.out.println("  Status: " + (driver.isAvailable() ? "Available" : "Unavailable"));
            InputUtil.printDivider();

            for (int i = 0; i < commands.size(); i++) {
                InputUtil.printMenuOption(i + 1, commands.get(i).getLabel());
            }
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(commands.size() + 1, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, commands.size() + 1);

            if (choice == commands.size() + 1) {
                InputUtil.printSuccess("Logged out successfully.");
                running = false;
            } else {
                try {
                    commands.get(choice - 1).execute();
                } catch (AppException e) {
                    InputUtil.printError(e.getMessage());
                }
            }
        }
    }

    private void viewAssignedOrders() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            InputUtil.printWarning("No active orders assigned to you.");
            return;
        }
        InputUtil.printNumberedList(activeOrders, "YOUR ACTIVE ORDERS");
    }

    private void updateOrderStatus() {
        List<Order> activeOrders = orderService.getActiveOrdersForDriver(driver.getId());
        if (activeOrders.isEmpty()) {
            InputUtil.printWarning("No active orders to update.");
            return;
        }
        InputUtil.printNumberedList(activeOrders, "ACTIVE ORDERS");

        String orderId = InputUtil.readString("  Enter order ID to update: ");
        System.out.println("  Select new status:");
        System.out.println("  1. PREPARING");
        System.out.println("  2. OUT_FOR_DELIVERY");
        System.out.println("  3. DELIVERED");
        int statusChoice = InputUtil.readInt("  Status: ", 1, 3);

        OrderStatus newStatus;
        switch (statusChoice) {
            case 1: newStatus = OrderStatus.PREPARING; break;
            case 2: newStatus = OrderStatus.OUT_FOR_DELIVERY; break;
            case 3: newStatus = OrderStatus.DELIVERED; break;
            default: newStatus = OrderStatus.PLACED; break;
        }

        orderService.updateOrderStatus(orderId, newStatus);
        InputUtil.printSuccess("Order " + orderId + " updated to: " + newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            InputUtil.printSuccess("Delivery completed! You are now Available for new orders.");
        }
    }

    private void viewDeliveryHistory() {
        List<Order> history = orderService.getOrdersByDriver(driver.getId());
        if (history.isEmpty()) {
            InputUtil.printWarning("No delivery history.");
            return;
        }
        InputUtil.printNumberedList(history, "DELIVERY HISTORY");
    }

    private void toggleAvailability() {
        driver.setAvailable(!driver.isAvailable());
        userService.updateDriver(driver);
        String status = driver.isAvailable() ? "Available" : "Unavailable";
        InputUtil.printSuccess("Your status is now: " + status);
    }
}
