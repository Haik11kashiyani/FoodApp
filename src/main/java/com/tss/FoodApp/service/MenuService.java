package com.tss.FoodApp.service;

import com.tss.FoodApp.enums.FoodCategory;
import com.tss.FoodApp.enums.CuisineType;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages menu items — CRUD + search + sort using Stream API.
 * SRP: Only handles menu operations.
 *
 * Stream API used heavily here:
 * Why Stream not for-loop? → Streams are concise, readable, and declarative.
 * .filter().collect() says WHAT we want, not HOW to loop.
 * For-loop alternative would need: create list, loop, if-check, add to list, return list — 5 lines vs 1.
 */
public class MenuService {

    private final Repository<MenuItem> menuRepo;

    public MenuService(Repository<MenuItem> menuRepo) {
        this.menuRepo = menuRepo;
    }

    // --- CRUD ---

    public MenuItem addItem(String name, double price, FoodCategory category, CuisineType cuisineType) {
        if (name == null || name.trim().isEmpty()) {
            throw new AppException("Menu item name cannot be empty.");
        }
        if (price <= 0) {
            throw new AppException("Price must be greater than zero.");
        }

        String id = IdGenerator.generateId();
        MenuItem item = new MenuItem(id, name, price, category, cuisineType);
        menuRepo.save(item);
        AppLogger.info("Menu item added: " + name + " | Rs. " + price + " | ID: " + id);
        return item;
    }

    public MenuItem updateItem(String id, String newName, double newPrice, FoodCategory newCategory, CuisineType newCuisineType) {
        MenuItem item = menuRepo.findById(id)
                .orElseThrow(() -> new AppException("Menu item not found: " + id));

        item.setName(newName);
        item.setPrice(newPrice);
        item.setCategory(newCategory);
        item.setCuisineType(newCuisineType);
        menuRepo.update(item);
        AppLogger.info("Menu item updated: " + newName + " | ID: " + id);
        return item;
    }

    public boolean deleteItem(String itemId) {
        boolean deleted = menuRepo.deleteById(itemId);
        if (!deleted) {
            throw new AppException("Menu item not found: " + itemId);
        }
        AppLogger.info("Menu item deleted | ID: " + itemId);
        return true;
    }

    // --- Query methods using Stream API ---

    /**
     * Get all menu items.
     */
    public List<MenuItem> getAllItems() {
        return menuRepo.findAll();
    }

    /**
     * Get onlyAvailableitems (for customer view).
     * Stream: filter by isAvailable== true
     */
    public List<MenuItem> getAvailableItems() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Search items by name (case-insensitive partial match).
     * Stream: filter with contains()
     */
    public List<MenuItem> searchByName(String keyword) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Filter items by category.
     * Stream: filter by enum equality
     */
    public List<MenuItem> filterByCategory(FoodCategory category) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<MenuItem> filterByCuisineAndCategory(CuisineType cuisine, FoodCategory category) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getCuisineType() == cuisine && item.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Sort items by price (low to high).
     * Stream: sorted with Comparator.comparingDouble()
     * Why comparingDouble not compareTo? → MenuItem doesn't implement Comparable.
     * We sort by different fields on demand, so Comparator is more flexible.
     */
    public List<MenuItem> sortByPriceAsc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice))
                .collect(Collectors.toList());
    }

    /**
     * Sort items by price (high to low).
     * Stream: sorted with reversed Comparator
     */
    public List<MenuItem> sortByPriceDesc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Sort items by name (alphabetical).
     */
    public List<MenuItem> sortByName() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }

    /**
     * Find a single item by ID.
     */
    public MenuItem getItemById(String itemId) {
        return menuRepo.findAll().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AppException("Menu item not found: " + itemId));
    }
}