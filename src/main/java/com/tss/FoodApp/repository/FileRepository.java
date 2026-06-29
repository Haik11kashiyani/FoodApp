package com.tss.FoodApp.repository;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.util.AppLogger;

import java.io.*;
import java.util.*;

public class FileRepository<T> implements Repository<T> {

    private final String filePath;
    private List<T> cache;
    private boolean isLoaded = false;

    public FileRepository(String filePath) {
        this.filePath = filePath;
        // Data is no longer loaded eagerly in the constructor.
        // It will be lazily loaded when first needed.
        AppLogger.info("Repository initialized lazily for: " + filePath);
    }

    /**
     * Lazy loading mechanism. Ensures data is loaded into cache 
     * only when it's actually requested for the first time.
     */
    private synchronized void ensureLoaded() {
        if (!isLoaded) {
            this.cache = loadFromFile();
            this.isLoaded = true;
            AppLogger.info("Data lazily loaded from " + filePath + " | Records: " + cache.size());
        }
    }

    @Override
    public synchronized T save(T entity) {
        ensureLoaded();
        cache.add(entity);
        saveToFile();
        AppLogger.info("Entity saved to " + filePath);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        ensureLoaded();
        return cache.stream()
                .filter(entity -> getEntityId(entity).equals(id))
                .findFirst();
    }

    @Override
    public synchronized List<T> findAll() {
        ensureLoaded();
        return new ArrayList<>(cache);
    }

    @Override
    public synchronized T update(T entity) {
        ensureLoaded();
        String id = getEntityId(entity);
        for (int i = 0; i < cache.size(); i++) {
            if (getEntityId(cache.get(i)).equals(id)) {
                cache.set(i, entity);
                saveToFile();
                AppLogger.info("Entity updated in " + filePath + " | ID: " + id);
                return entity;
            }
        }
        throw new AppException("Entity not found for update. ID: " + id);
    }

    @Override
    public synchronized boolean deleteById(String id) {
        ensureLoaded();
        boolean removed = cache.removeIf(entity -> getEntityId(entity).equals(id));
        if (removed) {
            saveToFile();
            AppLogger.info("Entity deleted from " + filePath + " | ID: " + id);
        }
        return removed;
    }

    private String getEntityId(T entity) {
        try {
            java.lang.reflect.Method method = entity.getClass().getMethod("getId");
            return (String) method.invoke(entity);
        } catch (Exception e) {
            throw new AppException("Entity does not have getId() method: " + entity.getClass().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            AppLogger.error("Failed to load data from " + filePath, e);
            return new ArrayList<>();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new ArrayList<>(cache));
            }
        } catch (IOException e) {
            AppLogger.error("Failed to save data to " + filePath, e);
            throw new AppException("Failed to save data: " + e.getMessage(), e);
        }
    }
}