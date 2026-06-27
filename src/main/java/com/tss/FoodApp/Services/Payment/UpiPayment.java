package com.tss.FoodApp.Services.Payment;

public class UpiPayment implements IPaymentStrategy{
    @Override
    public boolean processPayment(double amount) {
        return false;
    }
}
