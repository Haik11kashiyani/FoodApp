package com.tss.FoodApp.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.EntityNotFoundException;
import com.tss.FoodApp.exception.ValidationException;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.util.IdGenerator;

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

        String id = IdGenerator.generateId();
        MenuItem item = new MenuItem(id, name, price, category, cuisineType);
        menuRepo.save(item);
        AppLogger.info("Menu item added: " + name + " | Rs. " + price + " | ID: " + id);
        return item;
    }

    public MenuItem updateItem(String id, String newName, double newPrice, FoodCategory newCategory, CuisineType newCuisineType) {
        MenuItem item = menuRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem", id));

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
            throw new EntityNotFoundException("MenuItem", itemId);
        }
        AppLogger.info("Menu item deleted | ID: " + itemId);
        return true;
    }

    public List<MenuItem> getAllItems() {
        return menuRepo.findAll();
    }

    public List<MenuItem> getAvailableItems() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

    public List<MenuItem> searchByName(String keyword) {
        return menuRepo.findAll().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(MenuItem::isAvailable)
                .collect(Collectors.toList());
    }

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

    public List<MenuItem> sortByPriceAsc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice))
                .collect(Collectors.toList());
    }

    public List<MenuItem> sortByPriceDesc() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparingDouble(MenuItem::getPrice).reversed())
                .collect(Collectors.toList());
    }

    public List<MenuItem> sortByName() {
        return menuRepo.findAll().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }

    public MenuItem getItemById(String itemId) {
        return menuRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem", itemId));
    }
}
