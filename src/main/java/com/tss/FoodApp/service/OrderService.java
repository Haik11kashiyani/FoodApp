package com.tss.FoodApp.service;

import com.tss.FoodApp.enums.OrderStatus;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.enums.PaymentMode;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Handles order creation, status updates, delivery assignment, and invoice printing.
 * Combines what would be OrderService + DeliveryService + InvoiceService in a larger app.
 * Why combined? → For 33-file scope, these are closely related. If app grows, split them out.
 */
public class OrderService {

    private final Repository<Order> orderRepo;
    private final Repository<DeliveryPartner> driverRepo;
    private final Random random = new Random();

    public OrderService(Repository<Order> orderRepo, Repository<DeliveryPartner> driverRepo) {
        this.orderRepo = orderRepo;
        this.driverRepo = driverRepo;
    }

    /**
     * Create a new order from cart items.
     */
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

    /**
     * Randomly assign an active andAvailabledelivery partner.
     * Stream: filter active +availableDrivers, collect to list, pick random.
     * Why Random not round-robin? → Simpler, no need to track last assigned index.
     */
    public DeliveryPartner assignRandomDriver() {
        List<DeliveryPartner>availableDrivers = driverRepo.findAll().stream()
                .filter(d -> d.isActive() && d.isAvailable())
                .collect(Collectors.toList());

        if (availableDrivers.isEmpty()) {
            throw new AppException("No delivery partnersAvailableat the moment. Please try again later.");
        }

        DeliveryPartner selected =availableDrivers.get(random.nextInt(availableDrivers.size()));
        selected.setAvailable(false);  // Mark asBusy
        driverRepo.update(selected);
        AppLogger.info("Delivery partner assigned: " + selected.getName() + " | ID: " + selected.getId());
        return selected;
    }

    /**
     * Update order status (used by delivery partner).
     */
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepo.findAll().stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new AppException("Order not found: " + orderId));

        order.setStatus(newStatus);
        orderRepo.update(order);

        // If delivered, make driverAvailableagain
        if (newStatus == OrderStatus.DELIVERED) {
            markDriverAvailable(order.getDeliveryPartnerId());
        }

        AppLogger.info("Order " + orderId + " status updated to: " + newStatus);
        return order;
    }

    /**
     * Get all orders for a specific customer.
     * Stream: filter by customerId.
     */
    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())  // Latest first
                .collect(Collectors.toList());
    }

    /**
     * Get orders assigned to a specific delivery partner.
     */
    public List<Order> getOrdersByDriver(String driverId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getDeliveryPartnerId().equals(driverId))
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get active (non-delivered) orders for a driver.
     */
    public List<Order> getActiveOrdersForDriver(String driverId) {
        return orderRepo.findAll().stream()
                .filter(o -> o.getDeliveryPartnerId().equals(driverId))
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders (for admin view).
     */
    public List<Order> getAllOrders() {
        return orderRepo.findAll().stream()
                .sorted(Comparator.comparing(Order::getOrderedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Print a formatted invoice for an order.
     * Uses box-drawing characters for a professional look.
     */
    public void printInvoice(Order order) {
        String line = com.tss.FoodApp.util.InputUtil.repeat("═", 50);
        System.out.println("\n╔" + line + "╗");
        System.out.println("║" + centerText("INVOICE", 50) + "║");
        System.out.println("╠" + line + "╣");
        System.out.printf("║ Order ID    : %-35s ║%n", order.getId());
        System.out.printf("║ Customer    : %-35s ║%n", order.getCustomerName());
        System.out.printf("║ Date        : %-35s ║%n", order.getOrderedAt());
        System.out.println("╠" + line + "╣");
        System.out.println("║" + centerText("ITEMS", 50) + "║");
        System.out.println("╠" + line + "╣");

        for (CartItem item : order.getItems()) {
            String itemLine = String.format("  %-18s x%-3d  Rs. %8.2f", item.getItemName(), item.getQuantity(), item.getSubtotal());
            System.out.printf("║ %-48s ║%n", itemLine);
        }

        System.out.println("╠" + line + "╣");
        System.out.printf("║ Subtotal    : Rs. %-34.2f ║%n", order.getTotalAmount());
        System.out.printf("║ Discount    : -Rs. %-33.2f ║%n", order.getDiscountAmount());
        System.out.printf("║ %-48s ║%n", com.tss.FoodApp.util.InputUtil.repeat("─", 48));
        System.out.printf("║ TOTAL       : Rs. %-34.2f ║%n", order.getFinalAmount());
        System.out.println("╠" + line + "╣");
        System.out.printf("║ Payment     : %-35s ║%n", order.getPaymentMode());
        System.out.printf("║ Delivery By : %-35s ║%n", order.getDeliveryPartnerName());
        System.out.printf("║ Status      : %-35s ║%n", order.getStatus());
        System.out.println("╚" + line + "╝");
    }

    /**
     * Mark a driver asAvailableagain (afterDelivery completed).
     */
    private void markDriverAvailable(String driverId) {
        driverRepo.findAll().stream()
                .filter(d -> d.getId().equals(driverId))
                .findFirst()
                .ifPresent(driver -> {
                    driver.setAvailable(true);
                    driverRepo.update(driver);
                    AppLogger.info("Driver " + driver.getName() + " is nowAvailable");
                });
    }

    /**
     * Center text within a given width.
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return com.tss.FoodApp.util.InputUtil.repeat(" ", Math.max(0, padding)) + text + com.tss.FoodApp.util.InputUtil.repeat(" ", Math.max(0, width - padding - text.length()));
    }
}