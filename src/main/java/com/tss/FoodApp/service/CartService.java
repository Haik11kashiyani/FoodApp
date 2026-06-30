package com.tss.FoodApp.service;

import java.util.*;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.util.AppLogger;

public class CartService {
    private final Map<String, List<CartItem>> carts = new HashMap<>();

    public void addToCart(String customerId, MenuItem menuItem, int quantity) {
        List<CartItem> cart = carts.get(customerId);
        if (cart == null) {
            cart = new ArrayList<>();
            carts.put(customerId, cart);
        }

        CartItem existing = null;
        for (CartItem ci : cart) {
            if (ci.getMenuItemId().equals(menuItem.getId())) {
                existing = ci;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            AppLogger.info("Cart updated - increased quantity for: " + menuItem.getName());
        } else {
            cart.add(new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
            AppLogger.info("Item added to cart: " + menuItem.getName() + " x" + quantity);
        }
    }

    public void removeFromCart(String customerId, String menuItemId) {
        List<CartItem> cart = getCart(customerId);
        CartItem toRemove = null;
        for (CartItem ci : cart) {
            if (ci.getMenuItemId().equals(menuItemId)) {
                toRemove = ci;
                break;
            }
        }
        if (toRemove == null) {
            throw new EntityNotFoundException("CartItem", menuItemId);
        }
        cart.remove(toRemove);
        AppLogger.info("Item removed from cart | Customer: " + customerId);
    }

    public void updateQuantity(String customerId, String menuItemId, int newQuantity) {
        List<CartItem> cart = getCart(customerId);
        CartItem item = null;
        for (CartItem ci : cart) {
            if (ci.getMenuItemId().equals(menuItemId)) {
                item = ci;
                break;
            }
        }
        if (item == null) {
            throw new EntityNotFoundException("CartItem", menuItemId);
        }

        item.setQuantity(newQuantity);
        AppLogger.info("Cart quantity updated: " + item.getItemName() + " -> " + newQuantity);
    }

    public List<CartItem> getCart(String customerId) {
        List<CartItem> cart = carts.get(customerId);
        if (cart == null) {
            return new ArrayList<>();
        }
        return cart;
    }

    public double getCartTotal(String customerId) {
        double total = 0;
        for (CartItem ci : getCart(customerId)) {
            total += ci.getSubtotal();
        }
        return total;
    }

    public boolean isCartEmpty(String customerId) {
        return getCart(customerId).isEmpty();
    }

    public void clearCart(String customerId) {
        carts.remove(customerId);
        AppLogger.info("Cart cleared for customer: " + customerId);
    }
}
