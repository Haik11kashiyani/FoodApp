package com.tss.FoodApp;

import com.tss.FoodApp.model.*;
import com.tss.FoodApp.service.*;

import java.util.List;

public class IntegrationTestRunner {
    public static void main(String[] args) {
        System.out.println("Starting Integration Test Runner...");
        ServiceRegistry sr = new ServiceRegistry();

        AuthService auth = sr.getAuthService();
        MenuService menu = sr.getMenuService();
        UserService user = sr.getUserService();
        CartService cart = sr.getCartService();
        OrderService orderService = sr.getOrderService();
        OrderProcessor orderProcessor = sr.getOrderProcessor();

        try {
            System.out.println("Seeding default admin...");
            auth.seedDefaultAdmin();

            System.out.println("Registering sample driver...");
            DeliveryPartner dp = auth.registerDriver("driver1", "pass123", "Driver One", "9999999999", "Bike");
            System.out.println("Driver created: " + dp);

            System.out.println("Registering sample customer...");
            Customer c = auth.registerCustomer("cust1", "pass123", "Customer One", "8888888888", "123 Park");
            System.out.println("Customer created: " + c);

            System.out.println("Adding sample menu items...");
            MenuItem m1 = menu.addItem("Paneer Butter Masala", 250.0, FoodCategory.VEG, CuisineType.INDIAN);
            MenuItem m2 = menu.addItem("Margherita Pizza", 399.0, FoodCategory.VEG, CuisineType.ITALIAN);
            System.out.println("Menu items added: " + m1 + ", " + m2);

            System.out.println("Customer adds items to cart...");
            cart.addToCart(c.getId(), m1, 2);
            cart.addToCart(c.getId(), m2, 1);
            System.out.println("Cart total: " + cart.getCartTotal(c.getId()));

            System.out.println("Placing order (CASH)...");
            Order order = orderProcessor.placeOrder(c.getId(), c.getName(), PaymentMode.CASH, null);
            System.out.println("Order placed: " + order.getId());

            System.out.println("Updating order status to PREPARING...");
            orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
            System.out.println("Status now: " + orderService.getAllOrders().stream().filter(o -> o.getId().equals(order.getId())).findFirst().get().getStatus());

            System.out.println("Updating order status to OUT_FOR_DELIVERY...");
            orderService.updateOrderStatus(order.getId(), OrderStatus.OUT_FOR_DELIVERY);
            System.out.println("Updating order status to DELIVERED...");
            orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);

            System.out.println("Verifying repositories sizes...");
            System.out.println("Admins: " + sr.getAuthService().seedDefaultAdmin());
            System.out.println("Customers: " + user.getAllCustomers().size());
            System.out.println("Drivers: " + user.getAllDrivers().size());
            System.out.println("Menu items: " + menu.getAllItems().size());
            System.out.println("Orders: " + orderService.getAllOrders().size());

            System.out.println("Integration test completed successfully.");
        } catch (Exception e) {
            System.err.println("Integration test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
