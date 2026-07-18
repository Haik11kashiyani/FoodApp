package com.tss.FoodApp.service;

import java.util.*;
import com.tss.FoodApp.model.Cart;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;

public class CartService {
    private final Repository<Cart> cartRepo;

    public CartService(Repository<Cart> cartRepo) {
        this.cartRepo = cartRepo;
    }

    private Cart loadCart(Long customerId) {
        Cart c = cartRepo.findById(customerId);
        if (c == null) {
            c = new Cart(customerId);
            cartRepo.save(c);
        }
        return c;
    }

    public void addToCart(Long customerId, MenuItem menuItem, int quantity) {
        Cart cart = loadCart(customerId);
        List<CartItem> items = cart.getItems();

        CartItem existing = null;
        for (CartItem ci : items) {
            if (ci.getMenuItemId().equals(menuItem.getId())) {
                existing = ci;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            AppLogger.info("Cart updated - increased quantity for: " + menuItem.getName());
        } else {
            items.add(new CartItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), quantity));
            AppLogger.info("Item added to cart: " + menuItem.getName() + " x" + quantity);
        }
        cart.setItems(items);
        cartRepo.update(cart);
    }

    public void removeFromCart(Long customerId, Long menuItemId) {
        Cart cart = loadCart(customerId);
        List<CartItem> items = cart.getItems();
        CartItem toRemove = null;
        for (CartItem ci : items) {
            if (ci.getMenuItemId().equals(menuItemId)) {
                toRemove = ci;
                break;
            }
        }
        if (toRemove == null) {
            throw new EntityNotFoundException("CartItem", String.valueOf(menuItemId));
        }
        items.remove(toRemove);
        cart.setItems(items);
        cartRepo.update(cart);
        AppLogger.info("Item removed from cart | Customer: " + customerId);
    }

    public void updateQuantity(Long customerId, Long menuItemId, int newQuantity) {
        Cart cart = loadCart(customerId);
        List<CartItem> items = cart.getItems();
        CartItem item = null;
        for (CartItem ci : items) {
            if (ci.getMenuItemId().equals(menuItemId)) {
                item = ci;
                break;
            }
        }
        if (item == null) {
            throw new EntityNotFoundException("CartItem", String.valueOf(menuItemId));
        }

        item.setQuantity(newQuantity);
        cart.setItems(items);
        cartRepo.update(cart);
        AppLogger.info("Cart quantity updated: " + item.getItemName() + " -> " + newQuantity);
    }

    public List<CartItem> getCart(Long customerId) {
        Cart cart = cartRepo.findById(customerId);
        if (cart == null) return new ArrayList<>();
        return cart.getItems();
    }

    public double getCartTotal(Long customerId) {
        double total = 0;
        for (CartItem ci : getCart(customerId)) {
            total += ci.getSubtotal();
        }
        return total;
    }

    public boolean isCartEmpty(Long customerId) {
        return getCart(customerId).isEmpty();
    }

    public void clearCart(Long customerId) {
        cartRepo.deleteById(customerId);
        AppLogger.info("Cart cleared for customer: " + customerId);
    }
}
