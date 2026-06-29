package com.tss.FoodApp.exception;

public class EntityNotFoundException extends AppException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " not found: " + id);
    }
}
