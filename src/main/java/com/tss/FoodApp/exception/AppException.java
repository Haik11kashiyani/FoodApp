package com.tss.FoodApp.exception;

/**
 * Base custom exception for all application-specific errors.
 * Extends RuntimeException (unchecked) so we don't need 'throws' on every method.
 * Why RuntimeException not Exception? → Checked exceptions force try-catch at every level.
 * In our app, we catch exceptions only at the UI layer — RuntimeException propagates freely.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}