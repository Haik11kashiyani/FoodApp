package com.tss.FoodApp.util;

import java.util.UUID;

/**
 * Generates unique short IDs for all entities.
 * Uses first 8 characters of UUID → gives 16^8 = ~4 billion possible IDs.
 * Why UUID not auto-increment int? → UUID is globally unique, thread-safe, no shared counter needed.
 * Why only 8 chars? → Full UUID (36 chars) is too long for console display. 8 chars is readable.
 */
public class IdGenerator {

    private IdGenerator() {} // Prevent instantiation — all methods are static

    /**
     * Generates a unique 8-character uppercase ID.
     * Example output: "A3F2B1C9"
     */
    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}