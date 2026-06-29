package com.tss.FoodApp.ui;

import com.tss.FoodApp.enums.FoodCategory;
import com.tss.FoodApp.enums.CuisineType;
import com.tss.FoodApp.enums.PaymentMode;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.facade.OrderProcessingFacade;
import com.tss.FoodApp.factory.ServiceRegistry;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;
import com.tss.FoodApp.util.InputUtil;

import java.util.List;

/**
 *CUSTOMER DASHBOARD menu — browse menu, manage cart, place orders.
 * Receives logged-in Customer and ServiceRegistry.
 */
public class CustomerMenu {

    private final Customer customer;
    private final MenuService menuService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderProcessingFacade orderFacade;

    public CustomerMenu(User user, ServiceRegistry registry) {
        this.customer = (Customer) user;
        this.menuService = registry.getMenuService();
        this.cartService = registry.getCartService();
        this.orderService = registry.getOrderService();
        this.orderFacade = registry.getOrderFacade();
    }

    public void show() {
        boolean running = true;
        while (running) {
            InputUtil.printHeader("CUSTOMER DASHBOARD");
            System.out.println("  Welcome, " + customer.getName() + "!");
            InputUtil.printDivider();
            InputUtil.printMenuOption(1, "Menu");
            InputUtil.printMenuOption(2, "Cart");
            InputUtil.printMenuOption(3, "Orders");
            InputUtil.printMenuOption(4, "Account");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(5, "Logout");
            InputUtil.printDivider();

            int choice = InputUtil.readInt("  Enter choice: ", 1, 5);

            try {
                switch (choice) {
                    case 1: showMenuBrowsing(); break;
                    case 2: showCartOperations(); break;
                    case 3: showOrders(); break;
                    case 4: showAccount(); break;
                    case 5:
                        InputUtil.printSuccess("Logged out successfully.");
                        running = false;
                        break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showMenuBrowsing() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("MENU");
            InputUtil.printMenuOption(1, "View Menu");
            InputUtil.printMenuOption(2, "Search Menu by Name");
            InputUtil.printMenuOption(3, "Filter Menu by Category");
            InputUtil.printMenuOption(4, "Sort Menu by Price");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(5, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 5);
            try {
                switch (choice) {
                    case 1: viewMenu(); break;
                    case 2: searchMenu(); break;
                    case 3: filterByCategory(); break;
                    case 4: sortByPrice(); break;
                    case 5: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showCartOperations() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("CART");
            InputUtil.printMenuOption(1, "Add to Cart");
            InputUtil.printMenuOption(2, "View Cart");
            InputUtil.printMenuOption(3, "Remove from Cart");
            InputUtil.printMenuOption(4, "Update Item Quantity");
            InputUtil.printMenuOption(5, "Clear Cart");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(6, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 6);
            try {
                switch (choice) {
                    case 1: addToCart(); break;
                    case 2: viewCart(); break;
                    case 3: removeFromCart(); break;
                    case 4: updateCartQuantity(); break;
                    case 5: clearCart(); break;
                    case 6: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showOrders() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("ORDERS");
            InputUtil.printMenuOption(1, "Place Order");
            InputUtil.printMenuOption(2, "View Order History");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(3, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 3);
            try {
                switch (choice) {
                    case 1: placeOrder(); break;
                    case 2: viewOrderHistory(); break;
                    case 3: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    private void showAccount() {
        boolean back = false;
        while (!back) {
            InputUtil.printHeader("ACCOUNT");
            InputUtil.printMenuOption(1, "Update Profile");
            System.out.println("  ─────────────");
            InputUtil.printMenuOption(2, "Back");
            InputUtil.printDivider();
            int choice = InputUtil.readInt("  Enter choice: ", 1, 2);
            try {
                switch (choice) {
                    case 1: updateProfile(); break;
                    case 2: back = true; break;
                }
            } catch (AppException e) {
                InputUtil.printError(e.getMessage());
            }
        }
    }

    // ==================== Menu Browsing ====================

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

    // ==================== Cart Operations ====================

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

        int quantity = InputUtil.readInt("  Quantity (1-50): ", 1, 50);
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
        int newQty = InputUtil.readInt("  New quantity (1-50): ", 1, 50);
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

    // ==================== Order Placement ====================

    private void placeOrder() {
        InputUtil.printHeader("PLACE ORDER");
        viewCart();
        if (cartService.isCartEmpty(customer.getId())) return;

        System.out.printf("%n  Cart Total: Rs. %.2f%n", cartService.getCartTotal(customer.getId()));
        if (cartService.getCartTotal(customer.getId()) >= orderFacade.getDiscountThreshold()) {
            System.out.printf(" You're eligible for %.0f%% discount!%n", orderFacade.getDiscountPercentage());
        }

        PaymentMode mode = InputUtil.readEnum("\n  Select payment method", PaymentMode.class);

        if (InputUtil.readYesNo("  Confirm order?")) {
            // This is where the FACADE pattern shines — one call does everything
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

    // ==================== Profile ====================

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

        // Save updated customer
        ServiceRegistry.getInstance().getUserService().updateCustomer(customer);
        InputUtil.printSuccess("Profile updated!");
    }
}