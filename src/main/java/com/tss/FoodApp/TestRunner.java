package com.tss.FoodApp;

import java.util.List;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== STARTING INTEGRATION TEST RUN ===");
        try {
            ServiceRegistry registry = ServiceRegistry.getInstance();
            AuthService auth = registry.getAuthService();
            UserService userSvc = registry.getUserService();
            MenuService menu = registry.getMenuService();
            CartService cart = registry.getCartService();
            OrderService orderSvc = registry.getOrderService();
            OrderProcessingFacade orderFacade = registry.getOrderFacade();

            // 1. Seed default admin
            System.out.println("\n[STEP 1] Seeding Default Admin...");
            auth.seedDefaultAdmin();

            // 2. Register Delivery Driver
            System.out.println("\n[STEP 2] Registering Delivery Partner...");
            DeliveryPartner driver = auth.registerDriver("driver1", "driverpass", "John Driver", "9876543210", "Bike");
            System.out.println("Driver registered: " + driver);

            // 3. Register Customer
            System.out.println("\n[STEP 3] Registering Customer...");
            Customer customer = auth.registerCustomer("customer1", "custpass", "Alice Smith", "9887766554", "456 Main St");
            System.out.println("Customer registered: " + customer);

            // 4. Add Menu Items (Simulating Admin action)
            System.out.println("\n[STEP 4] Adding Menu Items...");
            // Clean up existing menu items first for consistent test run
            for (MenuItem item : menu.getAllItems()) {
                registry.getMenuRepo().deleteById(item.getId());
            }
            MenuItem pizza = menu.addItem("Margherita Pizza", 250.0, FoodCategory.VEG, CuisineType.ITALIAN);
            MenuItem burger = menu.addItem("Cheese Burger", 150.0, FoodCategory.VEG, CuisineType.INDIAN);
            System.out.println("Added: " + pizza);
            System.out.println("Added: " + burger);

            // 5. Add to Cart
            System.out.println("\n[STEP 5] Customer Adding Items to Cart...");
            cart.addToCart(customer.getId(), pizza, 2); // 500 Rs
            cart.addToCart(customer.getId(), burger, 1); // 150 Rs
            System.out.println("Cart Total: Rs. " + cart.getCartTotal(customer.getId()));

            // 6. Place Order
            System.out.println("\n[STEP 6] Placing Order (UPI Mode)...");
            // Set up UPI input simulation by setting driver availability to true
            driver.setAvailable(true);
            userSvc.updateDriver(driver);

            // Note: Since OrderProcessingFacade.placeOrder will prompt for UPI ID if UPI is selected,
            // we will simulate Cash payment instead to run fully automated!
            Order order = orderFacade.placeOrder(customer.getId(), customer.getName(), PaymentMode.CASH);
            System.out.println("Placed Order: " + order);

            // 7. Process Order Delivery
            System.out.println("\n[STEP 7] Updating Order Status to DELIVERED...");
            System.out.println("Driver availability BEFORE delivery: " + driver.isAvailable());
            orderSvc.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
            orderSvc.updateOrderStatus(order.getId(), OrderStatus.OUT_FOR_DELIVERY);
            orderSvc.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);

            // Reload driver state
            driver = userSvc.getDriverById(driver.getId()).get();
            System.out.println("Driver availability AFTER delivery: " + driver.isAvailable());

            System.out.println("\n=== INTEGRATION TEST RUN SUCCESSFUL! ===");
        } catch (Exception e) {
            System.out.println("\n!!! INTEGRATION TEST FAILED !!!");
            e.printStackTrace();
        }
    }
}
