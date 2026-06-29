package com.tss.FoodApp.service;

import com.tss.FoodApp.model.Order;
import com.tss.FoodApp.model.OrderStatus;

public interface OrderEventListener {
    void onStatusChanged(Order order, OrderStatus newStatus);
}
