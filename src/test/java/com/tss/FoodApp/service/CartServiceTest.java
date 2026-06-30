package com.tss.FoodApp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.model.CuisineType;
import com.tss.FoodApp.model.FoodCategory;
import com.tss.FoodApp.exception.EntityNotFoundException;
import java.util.List;

public class CartServiceTest {
    private CartService cartService;
    private MenuItem pizza;
    private MenuItem burger;

    @BeforeEach
    public void setUp() {
        cartService = new CartService();
        pizza = new MenuItem("item-1", "Pizza", 299.0, FoodCategory.VEG, CuisineType.ITALIAN, true);
        burger = new MenuItem("item-2", "Burger", 149.0, FoodCategory.NON_VEG, CuisineType.AMERICAN, true);
    }

    @Test
    public void testAddToCart_NewItem() {
        cartService.addToCart("cust-123", pizza, 2);
        List<CartItem> cart = cartService.getCart("cust-123");
        
        assertEquals(1, cart.size());
        assertEquals("Pizza", cart.get(0).getItemName());
        assertEquals(2, cart.get(0).getQuantity());
        assertEquals(598.0, cartService.getCartTotal("cust-123"), 0.01);
    }

    @Test
    public void testAddToCart_ExistingItem() {
        cartService.addToCart("cust-123", pizza, 2);
        cartService.addToCart("cust-123", pizza, 3);
        List<CartItem> cart = cartService.getCart("cust-123");

        assertEquals(1, cart.size());
        assertEquals(5, cart.get(0).getQuantity());
        assertEquals(1495.0, cartService.getCartTotal("cust-123"), 0.01);
    }

    @Test
    public void testRemoveFromCart() {
        cartService.addToCart("cust-123", pizza, 1);
        cartService.addToCart("cust-123", burger, 2);

        cartService.removeFromCart("cust-123", "item-1");
        List<CartItem> cart = cartService.getCart("cust-123");

        assertEquals(1, cart.size());
        assertEquals("Burger", cart.get(0).getItemName());
    }

    @Test
    public void testRemoveFromCart_NotFound() {
        assertThrows(EntityNotFoundException.class, () -> {
            cartService.removeFromCart("cust-123", "non-existent");
        });
    }

    @Test
    public void testUpdateQuantity() {
        cartService.addToCart("cust-123", pizza, 2);
        cartService.updateQuantity("cust-123", "item-1", 5);
        List<CartItem> cart = cartService.getCart("cust-123");

        assertEquals(5, cart.get(0).getQuantity());
        assertEquals(1495.0, cartService.getCartTotal("cust-123"), 0.01);
    }

    @Test
    public void testClearCart() {
        cartService.addToCart("cust-123", pizza, 2);
        assertFalse(cartService.isCartEmpty("cust-123"));

        cartService.clearCart("cust-123");
        assertTrue(cartService.isCartEmpty("cust-123"));
    }
}
