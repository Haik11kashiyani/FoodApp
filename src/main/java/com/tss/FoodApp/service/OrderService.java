package com.tss.FoodApp.service;

import java.util.*;
import java.util.stream.Collectors;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;
import com.tss.FoodApp.util.InputUtil;

public class OrderService {
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
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new ValidationException(
                    "Cannot change status from " + order.getStatus() + " to " + newStatus
                    + ". Next valid status: " + getNextValidStatus(order.getStatus()));
        }

        order.setStatus(newStatus);
        orderRepo.update(order);

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
