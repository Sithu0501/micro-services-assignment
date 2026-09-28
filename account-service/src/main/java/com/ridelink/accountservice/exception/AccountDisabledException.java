package com.ridelink.accountservice.exception;

/**
 * Thrown when an account has been suspended or deactivated and is barred from authentication.
 * Maps to HTTP 403 Forbidden.
 */
public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String message) {
        super(message);
    }
}
