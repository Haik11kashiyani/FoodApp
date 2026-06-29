package com.tss.FoodApp.enums;

/**
 * Tracks the lifecycle of an order.
 * Why 5 statuses? → Covers the complete delivery flow. Driver updates PLACED → PREPARING → OUT_FOR_DELIVERY → DELIVERED.
 */
public enum OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}