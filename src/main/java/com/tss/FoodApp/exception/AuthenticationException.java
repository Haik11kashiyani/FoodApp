package com.tss.FoodApp.exception;

/**
 * Thrown when login fails — wrong username, wrong password, or inactive account.
 * Why separate from AppException? → Allows UI to show login-specific error messages.
 * Example: catch(AuthenticationException e) → "Login failed" vs catch(AppException e) → "Something went wrong"
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}