package com.tss.FoodApp.repository;

import java.util.List;

public interface Repository<T extends Identifiable> {
    T save(T entity);
    T findById(String id);
    List<T> findAll();
    T update(T entity);
    boolean deleteById(String id);
}
