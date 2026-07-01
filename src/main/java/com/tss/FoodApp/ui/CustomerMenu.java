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
    private final OrderProcessor orderProcessor;
    private final UserService userService;

    public CustomerMenu(User user, ServiceRegistry registry) {
        this.customer = (Customer) user;
        this.menuService = registry.getMenuService();
        this.cartService = registry.getCartService();
        this.orderService = registry.getOrderService();
        this.orderProcessor = registry.getOrderProcessor();
        this.userService = registry.getUserService();
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== CUSTOMER DASHBOARD ===");
            System.out.println("  Welcome, " + customer.getName() + "!");
            System.out.println("---------------------------------------------");
            System.out.println("1. Menu");
            System.out.println("2. Cart");
            System.out.println("3. Orders");
            System.out.println("4. Account");
            System.out.println("5. Logout");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1:
                        showMenuBrowsing();
                        break;
                    case 2:
                        showCartOperations();
                        break;
                    case 3:
                        showOrders();
                        break;
                    case 4:
                        showAccount();
                        break;
                    case 5:
                        System.out.println("Logged out successfully.");
                        running = false;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showMenuBrowsing() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. View Menu");
            System.out.println("2. Search Menu by Name");
            System.out.println("3. Sort Menu by Price");
            System.out.println("4. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 4);
            try {
                switch (choice) {
                    case 1:
                        viewMenu();
                        break;
                    case 2:
                        searchMenu();
                        break;
                    case 3:
                        sortByPrice();
                        break;
                    case 4:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showCartOperations() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== CART ===");
            System.out.println("1. Add to Cart");
            System.out.println("2. View Cart");
            System.out.println("3. Remove from Cart");
            System.out.println("4. Update Item Quantity");
            System.out.println("5. Clear Cart");
            System.out.println("6. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 6);
            try {
                switch (choice) {
                    case 1:
                        addToCart();
                        break;
                    case 2:
                        viewCart();
                        break;
                    case 3:
                        removeFromCart();
                        break;
                    case 4:
                        updateCartQuantity();
                        break;
                    case 5:
                        clearCart();
                        break;
                    case 6:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showOrders() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== ORDERS ===");
            System.out.println("1. Place Order");
            System.out.println("2. View Order History");
            System.out.println("3. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 3);
            try {
                switch (choice) {
                    case 1:
                        placeOrder();
                        break;
                    case 2:
                        viewOrderHistory();
                        break;
                    case 3:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAccount() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== ACCOUNT ===");
            System.out.println("1. Update Profile");
            System.out.println("2. Back");
            System.out.println("---------------------------------------------");

            int choice = InputUtil.readInt("Enter choice: ", 1, 2);
            try {
                switch (choice) {
                    case 1:
                        updateProfile();
                        break;
                    case 2:
                        back = true;
                        break;
                }
            } catch (AppException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void viewMenu() {
        CuisineType cuisine = InputUtil.readCuisineType("Select Cuisine");
        FoodCategory category = InputUtil.readFoodCategory("Select Category");
        List<MenuItem> items = menuService.filterByCuisineAndCategory(cuisine, category);
        
        printItemsList(items, cuisine.name() + " " + category.name() + " MENU");
    }

    private void searchMenu() {
        String keyword = InputUtil.readString("Search for: ");
        List<MenuItem> results = menuService.searchByName(keyword);
        printItemsList(results, "SEARCH RESULTS for '" + keyword + "'");
    }



    private void sortByPrice() {
        System.out.println("1. Low to High");
        System.out.println("2. High to Low");
        int sortChoice = InputUtil.readInt("Sort order: ", 1, 2);
        List<MenuItem> sorted = (sortChoice == 1) ?
                menuService.sortByPriceAsc() : menuService.sortByPriceDesc();
        printItemsList(sorted, "MENU (SORTED BY PRICE)");
    }

    private void addToCart() {
        CuisineType cuisine = InputUtil.readCuisineType("Select Cuisine of Item");
        FoodCategory category = InputUtil.readFoodCategory("Select Category of Item");
        List<MenuItem> items = menuService.filterByCuisineAndCategory(cuisine, category);
        printItemsList(items, cuisine.name() + " " + category.name() + " MENU");

        if (items.isEmpty()) {
            System.out.println("Warning: No menu items available in this category.");
            return;
        }

        String itemId = InputUtil.readString("Enter item ID to add: ");
        MenuItem item = menuService.getItemById(itemId);

        if (!item.isAvailable()) {
            System.out.println("Error: This item is currently unavailable.");
            return;
        }

        int quantity = InputUtil.readInt("Quantity (1-50): ", 1, AppConfig.MAX_QUANTITY);
        cartService.addToCart(customer.getId(), item, quantity);
        System.out.println(item.getName() + " x" + quantity + " added to cart!");
    }

    private void viewCart() {
        List<CartItem> cart = cartService.getCart(customer.getId());
        if (cart.isEmpty()) {
            System.out.println("Warning: Your cart is empty.");
            return;
        }
        printItemsList(cart, "YOUR CART");
        System.out.printf("Cart Total: Rs. %.2f%n", cartService.getCartTotal(customer.getId()));
    }

    private void removeFromCart() {
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        String itemId = InputUtil.readString("Enter item ID to remove: ");
        cartService.removeFromCart(customer.getId(), itemId);
        System.out.println("Item removed from cart.");
    }

    private void updateCartQuantity() {
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        String itemId = InputUtil.readString("Enter item ID to update: ");
        int newQty = InputUtil.readInt("New quantity (1-50): ", 1, AppConfig.MAX_QUANTITY);
        cartService.updateQuantity(customer.getId(), itemId, newQty);
        System.out.println("Quantity updated.");
    }

    private void clearCart() {
        if (cartService.isCartEmpty(customer.getId())) {
            System.out.println("Warning: Cart is already empty.");
            return;
        }
        if (InputUtil.readYesNo("Clear entire cart?")) {
            cartService.clearCart(customer.getId());
            System.out.println("Cart cleared.");
        }
    }

    private void placeOrder() {
        System.out.println("\n=== PLACE ORDER ===");
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        double cartTotal = cartService.getCartTotal(customer.getId());
        System.out.printf("%nCart Total: Rs. %.2f%n", cartTotal);

        PercentageDiscount strategy = orderProcessor.getDiscountStrategy();
        double discountAmount = strategy.calculateDiscount(cartTotal);
        if (discountAmount > 0) {
            System.out.println("You're eligible! " + strategy.getDescription());
            System.out.printf("   You save: Rs. %.2f%n", discountAmount);
        }
        double finalAmount = cartTotal - discountAmount;

        PaymentMode mode = InputUtil.readPaymentMode("\nSelect payment method");
        String upiId = null;

        if (mode == PaymentMode.UPI) {
            System.out.println("\n  UPI Payment Selected");
            upiId = InputUtil.readLine("   Enter your UPI ID (e.g., name@upi): ");
            if (!upiId.contains("@")) {
                System.out.println("Error: Invalid UPI ID format. Must contain '@'.");
                return;
            }
            System.out.printf("   Processing payment of Rs. %.2f via UPI ID: %s%n", finalAmount, upiId);
            System.out.println("   UPI Payment Successful!");
        } else if (mode == PaymentMode.CASH) {
            System.out.println("\n  Cash Payment Selected");
            System.out.printf("   Amount to pay on delivery: Rs. %.2f%n", finalAmount);
            System.out.println("   Status: Payment will be collected on delivery.");
        }

        if (InputUtil.readYesNo("Confirm order?")) {
            Order order = orderProcessor.placeOrder(customer.getId(), customer.getName(), mode, upiId);
            System.out.println("Order placed successfully! Order ID: " + order.getId());
            InvoicePrinter.printInvoice(order);
        }
    }

    private void viewOrderHistory() {
        List<Order> orders = orderService.getOrdersByCustomer(customer.getId());
        if (orders.isEmpty()) {
            System.out.println("Warning: No orders found.");
            return;
        }
        printItemsList(orders, "YOUR ORDERS");

        if (InputUtil.readYesNo("View order details?")) {
            String orderId = InputUtil.readString("Enter order ID: ");
            Order foundOrder = null;
            for (Order o : orders) {
                if (o.getId().equals(orderId)) {
                    foundOrder = o;
                    break;
                }
            }
            if (foundOrder != null) {
                InvoicePrinter.printInvoice(foundOrder);
            } else {
                System.out.println("Error: Order not found.");
            }
        }
    }

    private void updateProfile() {
        System.out.println("\n=== UPDATE PROFILE ===");
        System.out.println("  Current Name    : " + customer.getName());
        System.out.println("  Current Phone   : " + customer.getPhone());
        System.out.println("  Current Address : " + customer.getAddress());
        System.out.println("---------------------------------------------");

        if (InputUtil.readYesNo("Update name?")) {
            customer.setName(InputUtil.readString("New name: "));
        }
        if (InputUtil.readYesNo("Update phone?")) {
            String phone = InputUtil.readValidPhone("New phone: ");
            if (phone != null) {
                customer.setPhone(phone);
            }
        }
        if (InputUtil.readYesNo("Update address?")) {
            customer.setAddress(InputUtil.readLine("New address: "));
        }

        userService.updateCustomer(customer);
        System.out.println("Profile updated!");
    }

    private <T> void printItemsList(List<T> items, String title) {
        if (items.isEmpty()) {
            System.out.println("Warning: No " + title.toLowerCase() + " found.");
            return;
        }
        System.out.println("---------------------------------------------");
        System.out.println(title + " (" + items.size() + " items)");
        System.out.println("---------------------------------------------");
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i).toString());
        }
        System.out.println("---------------------------------------------");
    }
}
