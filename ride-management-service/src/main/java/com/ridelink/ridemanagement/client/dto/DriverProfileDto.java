/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 */
package com.ridelink.ridemanagement.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ridelink.ridemanagement.model.Location;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown=true)
public record DriverProfileDto(String id, String userId, String licenseNumber, String licenseExpiryDate, String phoneNumber, String availabilityStatus, String serviceArea, Location currentLocation, Instant createdAt, Instant updatedAt) {
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.availabilityStatus);
    }
}

