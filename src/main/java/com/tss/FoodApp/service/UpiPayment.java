package com.tss.FoodApp.service;

import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;
import com.tss.FoodApp.exception.PaymentException;

public class UpiPayment implements IPaymentStrategy {
    @Override
    public void processPayment(double amount) {
        System.out.println("\n  UPI Payment Selected");
        String upiId = InputUtil.readLine("   Enter your UPI ID (e.g., name@upi): ");

        if (!upiId.contains("@")) {
            AppLogger.warn("Invalid UPI ID entered: " + upiId);
            throw new PaymentException("Invalid UPI ID format. Must contain '@'.");
        }

        System.out.printf("   Processing payment of Rs. %.2f via UPI ID: %s%n", amount, upiId);
        System.out.println("   UPI Payment Successful!");
        AppLogger.info("UPI payment processed for Rs. " + String.format("%.2f", amount) + " via " + upiId);
    }
}
