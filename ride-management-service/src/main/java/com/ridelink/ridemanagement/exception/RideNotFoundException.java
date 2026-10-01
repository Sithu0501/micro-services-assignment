/*
 * Decompiled with CFR 0.152.
 */
package com.ridelink.ridemanagement.exception;

public class RideNotFoundException
extends RuntimeException {
    public RideNotFoundException(String id) {
        super("Ride not found with ID: " + id);
    }
}

