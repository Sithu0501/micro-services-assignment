package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.response.VehicleResponse;
import com.ridelink.drivervehicleservice.exception.DuplicateResourceException;
import com.ridelink.drivervehicleservice.exception.ResourceNotFoundException;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.repository.DriverRepository;
import com.ridelink.drivervehicleservice.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service managing vehicle registrations, updates, deletions, and driver-vehicle ownership.
 */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Register a new vehicle associated with the authenticated driver.
     */
    public VehicleResponse registerVehicle(String userId, CreateVehicleRequest request) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId + ". Please create a driver profile first."));

        String registrationNumber = request.registrationNumber().trim().toUpperCase();

        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new DuplicateResourceException("A vehicle with registration number '" + registrationNumber + "' already exists");
        }

        Vehicle vehicle = new Vehicle(
                driver.getId(),
                registrationNumber,
                request.make().trim(),
                request.model().trim(),
                request.year(),
                request.color().trim(),
                request.vehicleType(),
                request.capacity()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle {} registered successfully with ID: {} for driver: {}", registrationNumber, saved.getId(), driver.getId());
        return VehicleResponse.fromEntity(saved);
    }

    /**
     * Retrieve all vehicles belonging to the authenticated driver.
     */
    public List<VehicleResponse> getVehiclesByUserId(String userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        return vehicleRepository.findByDriverId(driver.getId())
                .stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieve a specific vehicle by its ID.
     */
    public VehicleResponse getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        return VehicleResponse.fromEntity(vehicle);
    }

    /**
     * Update vehicle details, enforcing ownership by the authenticated driver.
     */
    public VehicleResponse updateVehicle(String userId, String vehicleId, UpdateVehicleRequest request) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        if (!vehicle.getDriverId().equals(driver.getId())) {
            throw new AccessDeniedException("Access denied: You do not own vehicle with ID: " + vehicleId);
        }

        if (request.make() != null && !request.make().isBlank()) {
            vehicle.setMake(request.make().trim());
        }
        if (request.model() != null && !request.model().isBlank()) {
            vehicle.setModel(request.model().trim());
        }
        if (request.year() != null) {
            vehicle.setYear(request.year());
        }
        if (request.color() != null && !request.color().isBlank()) {
            vehicle.setColor(request.color().trim());
        }
        if (request.vehicleType() != null) {
            vehicle.setVehicleType(request.vehicleType());
        }
        if (request.capacity() != null) {
            vehicle.setCapacity(request.capacity());
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle ID: {} updated successfully by driver: {}", vehicleId, driver.getId());
        return VehicleResponse.fromEntity(saved);
    }

    /**
     * Delete a vehicle, enforcing ownership. If the driver has no remaining vehicles,
     * their availability status is set to UNAVAILABLE.
     */
    public void deleteVehicle(String userId, String vehicleId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        if (!vehicle.getDriverId().equals(driver.getId())) {
            throw new AccessDeniedException("Access denied: You do not own vehicle with ID: " + vehicleId);
        }

        vehicleRepository.deleteById(vehicleId);
        log.info("Vehicle ID: {} deleted by driver: {}", vehicleId, driver.getId());

        // Check remaining vehicles
        List<Vehicle> remaining = vehicleRepository.findByDriverId(driver.getId());
        if (remaining.isEmpty() && driver.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
            driver.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
            driverRepository.save(driver);
            log.info("Driver ID: {} set to UNAVAILABLE due to having zero active vehicles", driver.getId());
        }
    }
}
