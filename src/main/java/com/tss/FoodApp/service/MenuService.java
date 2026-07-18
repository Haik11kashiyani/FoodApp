package com.tss.FoodApp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.util.AppLogger;

public class MenuService {
    private final Repository<MenuItem> menuRepo;

    public MenuService(Repository<MenuItem> menuRepo) {
        this.menuRepo = menuRepo;
    }

    public MenuItem addItem(String name, double price, FoodCategory category, CuisineType cuisineType) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Menu item name cannot be empty.");
        }
        if (price <= 0) {
            throw new ValidationException("Price must be greater than zero.");
        }

        MenuItem item = new MenuItem(null, name, price, category, cuisineType);
        menuRepo.save(item);
        AppLogger.info("Menu item added: " + name + " | Rs. " + price + " | Name: " + name);
        return item;
    }

    public MenuItem updateItem(Long id, String newName, double newPrice, FoodCategory newCategory, CuisineType newCuisineType) {
        MenuItem item = menuRepo.findById(id);
        if (item == null) {
            throw new EntityNotFoundException("MenuItem", String.valueOf(id));
        }

        item.setName(newName);
        item.setPrice(newPrice);
        item.setCategory(newCategory);
        item.setCuisineType(newCuisineType);
        menuRepo.update(item);
        AppLogger.info("Menu item updated: " + newName + " | ID: " + id);
        return item;
    }

    public boolean deleteItem(Long itemId) {
        boolean deleted = menuRepo.deleteById(itemId);
        if (!deleted) {
            throw new EntityNotFoundException("MenuItem", String.valueOf(itemId));
        }
        AppLogger.info("Menu item deleted | ID: " + itemId);
        return true;
    }

    public List<MenuItem> getAllItems() {
        return menuRepo.findAll();
    }

    public List<MenuItem> getAvailableItems() {
        List<MenuItem> available = new ArrayList<>();
        for (MenuItem item : menuRepo.findAll()) {
            if (item.isAvailable()) {
                available.add(item);
            }
        }
        return available;
    }

    public List<MenuItem> searchByName(String keyword) {
        List<MenuItem> results = new ArrayList<>();
        for (MenuItem item : menuRepo.findAll()) {
            if (item.isAvailable() && item.getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(item);
            }
        }
        return results;
    }



    public List<MenuItem> filterByCuisineAndCategory(CuisineType cuisine, FoodCategory category) {
        List<MenuItem> results = new ArrayList<>();
        for (MenuItem item : menuRepo.findAll()) {
            if (item.getCuisineType() == cuisine && item.getCategory() == category) {
                results.add(item);
            }
        }
        return results;
    }

    public List<MenuItem> sortByPriceAsc() {
        List<MenuItem> available = getAvailableItems();
        available.sort(new Comparator<MenuItem>() {
            @Override
            public int compare(MenuItem m1, MenuItem m2) {
                return Double.compare(m1.getPrice(), m2.getPrice());
            }
        });
        return available;
    }

    public List<MenuItem> sortByPriceDesc() {
        List<MenuItem> available = getAvailableItems();
        available.sort(new Comparator<MenuItem>() {
            @Override
            public int compare(MenuItem m1, MenuItem m2) {
                return Double.compare(m2.getPrice(), m1.getPrice());
            }
        });
        return available;
    }


    public MenuItem getItemById(Long itemId) {
        MenuItem item = menuRepo.findById(itemId);
        if (item == null) {
            throw new EntityNotFoundException("MenuItem", String.valueOf(itemId));
        }
        return item;
    }
}
