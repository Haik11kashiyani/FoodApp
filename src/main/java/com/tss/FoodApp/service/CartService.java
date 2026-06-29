package com.tss.FoodApp.service;

import java.util.*;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.util.AppLogger;

public class CartService {
    private final Map<String, List<CartItem>> carts = new HashMap<>();

    public void addToCart(String customerId, MenuItem menuItem, int quantity) {
        List<CartItem> cart = carts.computeIfAbsent(customerId, k -> new ArrayList<>());

        Optional<CartItem> existing = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            AppLogger.info("Cart updated - increased quantity for: " + menuItem.getName());
        } else {
            cart.add(new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
            AppLogger.info("Item added to cart: " + menuItem.getName() + " x" + quantity);
        }
    }

    public void removeFromCart(String customerId, String menuItemId) {
        List<CartItem> cart = getCart(customerId);
        boolean removed = cart.removeIf(ci -> ci.getMenuItemId().equals(menuItemId));
        if (!removed) {
            throw new EntityNotFoundException("CartItem", menuItemId);
        }
        AppLogger.info("Item removed from cart | Customer: " + customerId);
    }

    public void updateQuantity(String customerId, String menuItemId, int newQuantity) {
        List<CartItem> cart = getCart(customerId);
        CartItem item = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("CartItem", menuItemId));

        item.setQuantity(newQuantity);
        AppLogger.info("Cart quantity updated: " + item.getItemName() + " -> " + newQuantity);
    }

    public List<CartItem> getCart(String customerId) {
        return carts.getOrDefault(customerId, new ArrayList<>());
    }

    public double getCartTotal(String customerId) {
        return getCart(customerId).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public boolean isCartEmpty(String customerId) {
        return getCart(customerId).isEmpty();
    }

    public void clearCart(String customerId) {
        carts.remove(customerId);
        AppLogger.info("Cart cleared for customer: " + customerId);
    }
}
