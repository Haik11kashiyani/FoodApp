package com.tss.FoodApp;

/**
 * CUSTOM EXCEPTION HIERARCHY
 *
 * Why? Each exception type tells you EXACTLY what went wrong.
 * Instead of catching generic "AppException" everywhere, you can
 * handle specific errors differently.
 *
 * Hierarchy:
 *   RuntimeException
 *     └── AppException              (base — catch-all for app errors)
 *           ├── AuthenticationException   (login failures)
 *           ├── EntityNotFoundException   (item/order/user not found)
 *           ├── ValidationException       (bad input data)
 *           ├── PaymentException          (payment failed)
 *           └── DuplicateEntityException  (username already taken)
 */
public class Exceptions {
    private Exceptions() {} // Container class
}

// Base exception — all app exceptions extend this
class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Wrong password, inactive account, user not found during login
class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message);
    }
}

// Entity not found in repository (menu item, order, user)
class EntityNotFoundException extends AppException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " not found: " + id);
    }
}

// Bad input data (empty name, invalid price, invalid status transition)
class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message);
    }
}

// Payment processing failure (invalid UPI ID, payment declined)
class PaymentException extends AppException {
    public PaymentException(String message) {
        super(message);
    }
}

// Duplicate entry (username already exists)
class DuplicateEntityException extends AppException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
