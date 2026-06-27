package com.tss.FoodApp.Model.Orders;

import java.util.ArrayList;
import java.util.List;

public class Order {
    protected String orderId;
        protected String customerId;
        protected List<> items=new ArrayList<>();
    protected double totalAmount;
    protected double finalAmount;
    protected String paymentMode;
    protected String deliveryPartnerId;
    protected String status;
}
