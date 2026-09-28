package com.ridelink.drivervehicleservice.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing a Driver operational profile in RideLink.
 * Stored in the 'drivers' collection of 'ridelink_driver_db'.
 *
 * Each driver profile is linked to an Account Service user through the unique 'userId'
 * extracted from the JWT token.
 */
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Indexed(unique = true)
    private String licenseNumber;

    private String licenseExpiryDate;

    private String phoneNumber;

    private AvailabilityStatus availabilityStatus;

    private String serviceArea;

    private Location currentLocation;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Driver() {
    }

    public Driver(String id, String userId, String licenseNumber, String licenseExpiryDate,
                  String phoneNumber, AvailabilityStatus availabilityStatus, String serviceArea,
                  Location currentLocation) {
        this.id = id;
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.phoneNumber = phoneNumber;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : AvailabilityStatus.UNAVAILABLE;
        this.serviceArea = serviceArea;
        this.currentLocation = currentLocation;
    }

    public Driver(String userId, String licenseNumber, String licenseExpiryDate,
                  String phoneNumber, String serviceArea) {
        this(null, userId, licenseNumber, licenseExpiryDate, phoneNumber,
                AvailabilityStatus.UNAVAILABLE, serviceArea, null);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public void setLicenseExpiryDate(String licenseExpiryDate) {
        this.licenseExpiryDate = licenseExpiryDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        return Objects.equals(id, driver.id) && Objects.equals(userId, driver.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", licenseExpiryDate='" + licenseExpiryDate + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", availabilityStatus=" + availabilityStatus +
                ", serviceArea='" + serviceArea + '\'' +
                ", currentLocation=" + currentLocation +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
