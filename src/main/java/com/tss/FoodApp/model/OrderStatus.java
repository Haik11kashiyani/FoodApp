package com.tss.FoodApp.model;

public enum OrderStatus {
    PLACED(1),
    PREPARING(2),
    OUT_FOR_DELIVERY(3),
    DELIVERED(4),
    CANCELLED(5);

    private final int step;

    OrderStatus(int step) {
        this.step = step;
    }

    public boolean canTransitionTo(OrderStatus next) {
        if (this == DELIVERED || this == CANCELLED) return false;
        if (next == CANCELLED) return true;
        return next.step == this.step + 1;
    }
}
