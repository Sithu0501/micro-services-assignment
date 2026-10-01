/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description="Geographic location point")
public class Location {
    @Schema(description="Human-readable street or landmark address", example="SLIIT Malabe Campus, New Kandy Rd, Malabe")
    private String address;
    @Schema(description="WGS-84 Latitude (-90.0 to 90.0)", example="6.9147")
    private Double latitude;
    @Schema(description="WGS-84 Longitude (-180.0 to 180.0)", example="79.9729")
    private Double longitude;

    public Location() {
    }

    public Location(String address, Double latitude, Double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Location location = (Location)o;
        return Objects.equals(this.address, location.address) && Objects.equals(this.latitude, location.latitude) && Objects.equals(this.longitude, location.longitude);
    }

    public int hashCode() {
        return Objects.hash(this.address, this.latitude, this.longitude);
    }

    public String toString() {
        return "Location{address='" + this.address + "', latitude=" + this.latitude + ", longitude=" + this.longitude + "}";
    }
}

