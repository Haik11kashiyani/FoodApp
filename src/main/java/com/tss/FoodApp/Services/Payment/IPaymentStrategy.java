package com.tss.FoodApp.Services.Payment;

public interface IPaymentStrategy {
    boolean processPayment(double amount);
}
