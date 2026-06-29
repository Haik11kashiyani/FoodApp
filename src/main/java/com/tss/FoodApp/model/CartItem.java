package com.tss.FoodApp.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String menuItemId;
    private String itemName;
    private double price;
    private int quantity;

    public CartItem(String menuItemId, String itemName, double price, int quantity) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return String.format("%-20s | Rs. %-8.2f | Qty: %-3d | Subtotal: Rs. %.2f", itemName, price, quantity, getSubtotal());
    }
}