package com.tss.FoodApp.repository;

import java.io.*;
import java.util.*;
import com.tss.FoodApp.util.AppLogger;
import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.exception.EntityNotFoundException;

public class FileRepository<T extends Identifiable> implements Repository<T> {

    private final String filePath;
    private final Map<String, T> cache;

    public FileRepository(String filePath) {
        this.filePath = filePath;
        this.cache = loadFromFile();
        AppLogger.info("Repository initialized for: " + filePath + " | Records: " + cache.size());
    }

    @Override
    public T save(T entity) {
        cache.put(getEntityId(entity), entity);
        saveToFile();
        AppLogger.info("Entity saved to " + filePath);
        return entity;
    }

    @Override
    public T findById(String id) {
        return cache.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public T update(T entity) {
        String id = getEntityId(entity);
        if (cache.containsKey(id)) {
            cache.put(id, entity);
            saveToFile();
            AppLogger.info("Entity updated in " + filePath + " | ID: " + id);
            return entity;
        }
        throw new EntityNotFoundException("Entity", id);
    }

    @Override
    public boolean deleteById(String id) {
        T removed = cache.remove(id);
        if (removed != null) {
            saveToFile();
            AppLogger.info("Entity deleted from " + filePath + " | ID: " + id);
            return true;
        }
        return false;
    }

    private String getEntityId(T entity) {
        return entity.getId();
    }

    @SuppressWarnings("unchecked")
    private Map<String, T> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<T> list = (List<T>) ois.readObject();
            Map<String, T> map = new HashMap<>();
            for (T entity : list) {
                map.put(getEntityId(entity), entity);
            }
            return map;
        } catch (Exception e) {
            AppLogger.error("Failed to load data from " + filePath, e);
            return new HashMap<>();
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
                oos.writeObject(new ArrayList<>(cache.values()));
            }
        } catch (IOException e) {
            AppLogger.error("Failed to save data to " + filePath, e);
            throw new AppException("Failed to save data: " + e.getMessage(), e);
        }
    }
}
