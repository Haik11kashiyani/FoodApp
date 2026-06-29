package com.tss.FoodApp.enums;

/**
 * Defines user roles in the system.
 * Why enum not String constants? → Type-safe, compiler catches typos, works with switch.
 */
public enum Role {
    ADMIN,
    CUSTOMER,
    DELIVERY_PARTNER
}