package com.tss.FoodApp.Services.Payment;

public class CashPayment implements IPaymentStrategy{
    @Override
    public boolean processPayment(double amount) {
        return false;
    }
}
