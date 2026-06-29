package com.tss.FoodApp;

import java.io.*;
import java.util.*;

interface Repository<T> {
    T save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    T update(T entity);
    boolean deleteById(String id);
}

class FileRepository<T> implements Repository<T> {

    private final String filePath;
    private Map<String, T> cache;
    private boolean isLoaded = false;

    public FileRepository(String filePath) {
        this.filePath = filePath;
        AppLogger.info("Repository initialized lazily for: " + filePath);
    }

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
        cache.put(getEntityId(entity), entity);
        saveToFile();
        AppLogger.info("Entity saved to " + filePath);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(String id) {
        ensureLoaded();
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        ensureLoaded();
        return new ArrayList<>(cache.values());
    }

    @Override
    public synchronized T update(T entity) {
        ensureLoaded();
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
    public synchronized boolean deleteById(String id) {
        ensureLoaded();
        T removed = cache.remove(id);
        if (removed != null) {
            saveToFile();
            AppLogger.info("Entity deleted from " + filePath + " | ID: " + id);
            return true;
        }
        return false;
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
    private Map<String, T> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new java.util.concurrent.ConcurrentHashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<T> list = (List<T>) ois.readObject();
            Map<String, T> map = new java.util.concurrent.ConcurrentHashMap<>();
            for (T entity : list) {
                map.put(getEntityId(entity), entity);
            }
            return map;
        } catch (Exception e) {
            AppLogger.error("Failed to load data from " + filePath, e);
            return new java.util.concurrent.ConcurrentHashMap<>();
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
