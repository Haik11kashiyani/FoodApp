package com.tss.FoodApp.service;

import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.InputUtil;

/**
 * UPI payment strategy — simulates UPI transaction.
 * Asks for UPI ID and validates format.
 * Implements IPaymentStrategy (Strategy Pattern).
 */
public class UpiPayment implements IPaymentStrategy {

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n📱 UPI Payment Selected");
        String upiId = InputUtil.readLine("   Enter your UPI ID (e.g., name@upi): ");

        // Simple validation: must contain @
        if (!upiId.contains("@")) {
            System.out.println("   ❌ Invalid UPI ID format. Must contain '@'.");
            AppLogger.warn("Invalid UPI ID entered: " + upiId);
            return false;
        }

        System.out.printf("   Processing payment of Rs. %.2f via UPI ID: %s%n", amount, upiId);
        System.out.println("   ✅ UPI Payment Successful!");
        AppLogger.info("UPI payment processed for Rs. " + String.format("%.2f", amount) + " via " + upiId);
        return true;
    }
}