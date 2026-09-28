package com.ridelink.accountservice.exception;

/**
 * Thrown when an incoming request fails domain-level validation or business rules
 * (e.g. attempting to self-assign ADMIN role during public registration).
 * Maps to HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
