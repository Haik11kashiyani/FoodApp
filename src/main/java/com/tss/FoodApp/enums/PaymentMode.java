package com.tss.FoodApp.enums;

/**
 * Supported payment methods.
 * Why enum? → Only 2 fixed options. Easy to add WALLET later without changing existing code (Open/Closed).
 */
public enum PaymentMode {
    CASH,
    UPI
}