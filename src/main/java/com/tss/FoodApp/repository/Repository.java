package com.tss.FoodApp.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T extends Identifiable> {
    T save(T entity);
    Optional<T> findById(String id);
    List<T> findAll();
    T update(T entity);
    boolean deleteById(String id);
}
