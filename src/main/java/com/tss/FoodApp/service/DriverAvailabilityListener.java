package com.tss.FoodApp.service;

import com.tss.FoodApp.model.DeliveryPartner;
import com.tss.FoodApp.model.Order;
import com.tss.FoodApp.model.OrderStatus;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;

public class DriverAvailabilityListener implements OrderEventListener {
    private final Repository<DeliveryPartner> driverRepo;

    public DriverAvailabilityListener(Repository<DeliveryPartner> driverRepo) {
        this.driverRepo = driverRepo;
    }

    @Override
    public void onStatusChanged(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.DELIVERED) {
            driverRepo.findById(order.getDeliveryPartnerId())
                .ifPresent(driver -> {
                    driver.setAvailable(true);
                    driverRepo.update(driver);
                    AppLogger.info("Driver " + driver.getName() + " marked available after delivery");
                });
        }
    }
}
