package com.tss.FoodApp.config;

/**
 * Centralized configuration constants for the FoodApp.
 * All file paths, default values, and settings are defined here.
 * Why class with static finals? → Simple, no instantiation needed. Alternative: properties file — adds file I/O complexity.
 */
public final class AppConfig {
    // Private constructor prevents instantiation
    private AppConfig() {}

    // Data file paths (relative to project root)
    public static final String DATA_DIR = "data";
    public static final String LOG_DIR = "logs";
    public static final String ADMIN_FILE = DATA_DIR + "/admins.dat";
    public static final String CUSTOMER_FILE = DATA_DIR + "/customers.dat";
    public static final String DELIVERY_PARTNER_FILE = DATA_DIR + "/delivery_partners.dat";
    public static final String MENU_FILE = DATA_DIR + "/menu_items.dat";
    public static final String ORDER_FILE = DATA_DIR + "/orders.dat";
    public static final String LOG_FILE = LOG_DIR + "/app.log";

    // Default admin credentials (seeded on first launch)
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_ADMIN_NAME = "System Admin";

    // Discount settings (admin can change at runtime, but these are defaults)
    public static final double DEFAULT_DISCOUNT_PERCENTAGE = 10.0;
    public static final double DEFAULT_DISCOUNT_THRESHOLD = 500.0;

    // Validation limits
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final double MAX_PRICE = 10000.0;
    public static final int MAX_QUANTITY = 50;
}