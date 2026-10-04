/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 */
package com.ridelink.ridemanagement.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ridelink.ridemanagement.model.Location;

@JsonIgnoreProperties(ignoreUnknown=true)
public record EligibleDriverDto(String driverId, String userId, String licenseNumber, String phoneNumber, String availabilityStatus, String serviceArea, Location currentLocation, Double distanceKm, VehicleDto vehicle) {
    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.availabilityStatus);
    }
}

