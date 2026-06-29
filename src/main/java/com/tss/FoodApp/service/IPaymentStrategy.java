package com.tss.FoodApp.service;

/**
 * Strategy interface for payment processing.
 * WHY STRATEGY PATTERN?
 * → Without it, PaymentService would have: if (mode == CASH) { ... } else if (mode == UPI) { ... }
 *   Adding WalletPayment means modifying PaymentService — violates Open/Closed Principle.
 *   With Strategy: create WalletPayment implements IPaymentStrategy — zero changes to existing code.
 *
 * Why interface not abstract class? → No shared state or default behavior needed.
 * Each payment type has completely different logic.
 */
public interface IPaymentStrategy {

    /**
     * Process payment for the given amount.
     * @param amount The amount to charge
     * @return true if payment successful, false otherwise
     */
    boolean processPayment(double amount);
}