/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 */
package com.ridelink.ridemanagement.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown=true)
public record AccountUserDto(String id, String fullName, String email, String phone, String role, String status, Instant createdAt, Instant updatedAt) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}

