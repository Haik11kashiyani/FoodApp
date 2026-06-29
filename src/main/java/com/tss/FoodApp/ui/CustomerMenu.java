package com.tss.FoodApp.ui;

import java.util.ArrayList;
import java.util.List;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.InputUtil;
import com.tss.FoodApp.ServiceRegistry;

public class CustomerMenu {
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
