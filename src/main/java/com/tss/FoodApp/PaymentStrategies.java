package com.tss.FoodApp;

/**
 * STRATEGY PATTERN for Payments (OCP)
 *
 * To add a new payment method (e.g., WalletPayment):
 *   1. Create: class WalletPayment implements IPaymentStrategy { ... }
 *   2. Register in ServiceRegistry: paymentStrategies.put(PaymentMode.WALLET, new WalletPayment());
 *   3. Add enum: WALLET in PaymentMode
 *   That's it — zero changes to existing code!
 */

interface IPaymentStrategy {
    void processPayment(double amount);
}

class CashPayment implements IPaymentStrategy {
    @Override
    public void processPayment(double amount) {
        System.out.println("\n  Cash Payment Selected");
        System.out.printf("   Amount to pay on delivery: Rs. %.2f%n", amount);
        System.out.println("   Status: Payment will be collected on delivery.");
        AppLogger.info("Cash payment processed for Rs. " + String.format("%.2f", amount));
    }
}

class UpiPayment implements IPaymentStrategy {
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
