package com.tss.FoodApp.enums;

/**
 * Categories for menu items.
 * Why enum not String? → Fixed set of categories. Prevents typos like "Veg" vs "veg" vs "VEG".
 */
public enum FoodCategory {
    VEG,
    NON_VEG
}