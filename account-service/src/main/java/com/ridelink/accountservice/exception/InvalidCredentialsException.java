package com.ridelink.accountservice.exception;

/**
 * Thrown when user authentication fails due to invalid email or password.
 * Maps to HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
