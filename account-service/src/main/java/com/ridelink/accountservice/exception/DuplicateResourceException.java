package com.ridelink.accountservice.exception;

/**
 * Thrown when attempting to create or update a resource with a conflicting unique key (e.g. existing email).
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
