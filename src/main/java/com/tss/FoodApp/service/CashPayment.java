package com.tss.FoodApp.service;

import com.tss.FoodApp.util.AppLogger;

/**
 * Cash payment strategy — simulates cash-on-delivery payment.
 * Implements IPaymentStrategy (Strategy Pattern).
 */
public class CashPayment implements IPaymentStrategy {

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n💵 Cash Payment Selected");
        System.out.printf("   Amount to pay on delivery: Rs. %.2f%n", amount);
        System.out.println("   Status: Payment will be collected on delivery.");
        AppLogger.info("Cash payment processed for Rs. " + String.format("%.2f", amount));
        return true;
    }
}