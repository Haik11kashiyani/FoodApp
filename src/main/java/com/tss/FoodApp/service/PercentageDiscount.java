package com.tss.FoodApp.service;

public class PercentageDiscount {
    private final double percentage;
    private final double threshold;

    public PercentageDiscount(double percentage, double threshold) {
        this.percentage = percentage;
        this.threshold = threshold;
    }

    public double calculateDiscount(double totalAmount) {
        return (totalAmount >= threshold) ? totalAmount * (percentage / 100.0) : 0;
    }

    public String getDescription() {
        return String.format("%.0f%% off on orders above Rs. %.0f", percentage, threshold);
    }

    public double getPercentage() { return percentage; }
    public double getThreshold() { return threshold; }
}
