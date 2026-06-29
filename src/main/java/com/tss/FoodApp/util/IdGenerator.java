package com.tss.FoodApp.util;

import java.util.UUID;

public class IdGenerator {
    private IdGenerator() {}

    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
