package com.tss.FoodApp.ui;

import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.Order;
import com.tss.FoodApp.util.InputUtil;

public class InvoicePrinter {

    public static void printInvoice(Order order) {
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

    private static String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return InputUtil.repeat(" ", Math.max(0, padding)) + text + InputUtil.repeat(" ", Math.max(0, width - padding - text.length()));
    }
}
