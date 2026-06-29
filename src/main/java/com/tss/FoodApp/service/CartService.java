package com.tss.FoodApp.service;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.util.AppLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages in-memory shopping carts for customers.
 * Why in-memory (HashMap) not serialized to file?
 * → Cart is temporary — customer adds items, places order, cart is cleared.
 *   Persisting carts adds complexity with no real benefit for this app.
 *   If needed later, just serialize the Map — architecture supports it.
 *
 * Why Map<String, List<CartItem>> not just List<CartItem>?
 * → Multiple customers might login in one app session (logout → login as different user).
 *   Map keeps each customer's cart separate, keyed by customerId.
 *
 * Why HashMap not TreeMap? → We lookup by customerId (exact key match).
 *   HashMap is O(1) lookup. TreeMap is O(log n) — we don't need sorted keys.
 */
public class CartService {

    // customerId → list of cart items
    private final Map<String, List<CartItem>> carts = new HashMap<>();

    /**
     * Add an item to the customer's cart.
     * If item already in cart, increases quantity.
     */
    public void addToCart(String customerId, MenuItem menuItem, int quantity) {
        List<CartItem> cart = carts.computeIfAbsent(customerId, k -> new ArrayList<>());

        // Check if item already in cart — if so, update quantity
        Optional<CartItem> existing = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
            AppLogger.info("Cart updated — increased quantity for: " + menuItem.getName());
        } else {
            cart.add(new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
            AppLogger.info("Item added to cart: " + menuItem.getName() + " x" + quantity);
        }
    }

    /**
     * Remove an item from the cart by menu item ID.
     */
    public void removeFromCart(String customerId, String menuItemId) {
        List<CartItem> cart = getCart(customerId);
        boolean removed = cart.removeIf(ci -> ci.getMenuItemId().equals(menuItemId));
        if (!removed) {
            throw new AppException("Item not found in cart.");
        }
        AppLogger.info("Item removed from cart | Customer: " + customerId);
    }

    /**
     * Update quantity of a cart item.
     */
    public void updateQuantity(String customerId, String menuItemId, int newQuantity) {
        List<CartItem> cart = getCart(customerId);
        CartItem item = cart.stream()
                .filter(ci -> ci.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new AppException("Item not found in cart."));

        item.setQuantity(newQuantity);
        AppLogger.info("Cart quantity updated: " + item.getItemName() + " → " + newQuantity);
    }

    /**
     * Get all items in a customer's cart.
     */
    public List<CartItem> getCart(String customerId) {
        return carts.getOrDefault(customerId, new ArrayList<>());
    }

    /**
     * Calculate total price of all items in cart.
     * Stream: mapToDouble + sum — concise one-liner.
     * Alternative: for-loop with accumulator variable — 4 lines instead of 1.
     */
    public double getCartTotal(String customerId) {
        return getCart(customerId).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    /**
     * Check if cart is empty.
     */
    public boolean isCartEmpty(String customerId) {
        return getCart(customerId).isEmpty();
    }

    /**
     * Clear the entire cart (after placing order).
     */
    public void clearCart(String customerId) {
        carts.remove(customerId);
        AppLogger.info("Cart cleared for customer: " + customerId);
    }
}