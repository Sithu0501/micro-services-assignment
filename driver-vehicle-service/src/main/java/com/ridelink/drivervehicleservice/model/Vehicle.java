package com.ridelink.drivervehicleservice.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing a Vehicle in the RideLink platform.
 * Stored in the 'vehicles' collection of 'ridelink_driver_db'.
 *
 * Each vehicle is associated with a specific driver via 'driverId'.
 */
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    @Indexed
    private String driverId;

    @Indexed(unique = true)
    private String registrationNumber;

    private String make;

    private String model;

    private int year;

    private String color;

    private VehicleType vehicleType;

    private int capacity;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Vehicle() {
    }

    public Vehicle(String id, String driverId, String registrationNumber, String make,
                   String model, int year, String color, VehicleType vehicleType, int capacity) {
        this.id = id;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.vehicleType = vehicleType != null ? vehicleType : VehicleType.SEDAN;
        this.capacity = capacity;
    }

    public Vehicle(String driverId, String registrationNumber, String make,
                   String model, int year, String color, VehicleType vehicleType, int capacity) {
        this(null, driverId, registrationNumber, make, model, year, color, vehicleType, capacity);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id) &&
               Objects.equals(registrationNumber, vehicle.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, registrationNumber);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", driverId='" + driverId + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", color='" + color + '\'' +
                ", vehicleType=" + vehicleType +
                ", capacity=" + capacity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
