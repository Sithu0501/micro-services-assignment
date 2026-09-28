package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicleservice.dto.response.DriverResponse;
import com.ridelink.drivervehicleservice.dto.response.EligibleDriverResponse;
import com.ridelink.drivervehicleservice.dto.response.VehicleResponse;
import com.ridelink.drivervehicleservice.exception.BadRequestException;
import com.ridelink.drivervehicleservice.exception.DuplicateResourceException;
import com.ridelink.drivervehicleservice.exception.ResourceNotFoundException;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Location;
import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.repository.DriverRepository;
import com.ridelink.drivervehicleservice.repository.VehicleRepository;
import com.ridelink.drivervehicleservice.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service managing Driver operational profiles, availability states,
 * simulated GPS positions, and eligible driver matching for ride dispatch.
 */
@Service
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverService(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Create a new driver operational profile linked to an authenticated Account Service user.
     */
    public DriverResponse createDriverProfile(String userId, CreateDriverRequest request) {
        if (driverRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("A driver profile already exists for user ID: " + userId);
        }

        if (driverRepository.existsByLicenseNumber(request.licenseNumber().trim())) {
            throw new DuplicateResourceException("Driver license number is already registered: " + request.licenseNumber());
        }

        Driver driver = new Driver(
                userId,
                request.licenseNumber().trim(),
                request.licenseExpiryDate().trim(),
                request.phoneNumber().trim(),
                request.serviceArea().trim()
        );

        Driver saved = driverRepository.save(driver);
        log.info("Driver profile created successfully with ID: {} for user: {}", saved.getId(), userId);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Retrieve the operational driver profile belonging to the authenticated user.
     */
    public DriverResponse getDriverProfileByUserId(String userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));
        return DriverResponse.fromEntity(driver);
    }

    /**
     * Retrieve a driver profile by its internal MongoDB ID (used for inter-service communication).
     */
    public DriverResponse getDriverProfileById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found with ID: " + id));
        return DriverResponse.fromEntity(driver);
    }

    /**
     * Update editable operational details of the driver's profile.
     */
    public DriverResponse updateDriverProfile(String userId, UpdateDriverRequest request) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        if (request.licenseExpiryDate() != null && !request.licenseExpiryDate().isBlank()) {
            driver.setLicenseExpiryDate(request.licenseExpiryDate().trim());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            driver.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.serviceArea() != null && !request.serviceArea().isBlank()) {
            driver.setServiceArea(request.serviceArea().trim());
        }

        Driver saved = driverRepository.save(driver);
        log.info("Driver profile updated for user: {}", userId);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Update driver availability status (AVAILABLE, UNAVAILABLE, ON_TRIP).
     * Enforces that a driver must have at least one registered vehicle before becoming AVAILABLE.
     */
    public DriverResponse updateAvailabilityStatus(String userId, AvailabilityStatus newStatus) {
        if (newStatus == null) {
            throw new BadRequestException("Availability status cannot be null");
        }

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        if (newStatus == AvailabilityStatus.AVAILABLE) {
            List<Vehicle> vehicles = vehicleRepository.findByDriverId(driver.getId());
            if (vehicles.isEmpty()) {
                throw new BadRequestException("Driver cannot be set to AVAILABLE without at least one registered vehicle");
            }
        }

        driver.setAvailabilityStatus(newStatus);
        Driver saved = driverRepository.save(driver);
        log.info("Driver availability updated to {} for user: {}", newStatus, userId);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Update driver simulated GPS coordinates.
     */
    public DriverResponse updateCurrentLocation(String userId, UpdateLocationRequest request) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));

        Location newLocation = new Location(request.latitude(), request.longitude());
        driver.setCurrentLocation(newLocation);

        Driver saved = driverRepository.save(driver);
        log.info("Driver location updated to ({}, {}) for user: {}", request.latitude(), request.longitude(), userId);
        return DriverResponse.fromEntity(saved);
    }

    /**
     * Retrieve eligible available drivers for ride dispatch.
     * Optionally filters by service area and/or calculates distance from a pickup coordinate.
     */
    public List<EligibleDriverResponse> findEligibleDrivers(
            String serviceArea,
            Double latitude,
            Double longitude,
            Double radiusKm
    ) {
        List<Driver> drivers;
        if (serviceArea != null && !serviceArea.isBlank()) {
            drivers = driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(
                    AvailabilityStatus.AVAILABLE,
                    serviceArea.trim()
            );
        } else {
            drivers = driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        }

        List<EligibleDriverResponse> eligibleList = new ArrayList<>();
        double effectiveRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : 10.0;

        for (Driver driver : drivers) {
            List<Vehicle> vehicles = vehicleRepository.findByDriverId(driver.getId());
            if (vehicles.isEmpty()) {
                continue; // Skip drivers without a registered vehicle
            }

            Vehicle primaryVehicle = vehicles.get(0);
            Double distanceKm = null;

            if (latitude != null && longitude != null && driver.getCurrentLocation() != null) {
                distanceKm = GeoUtils.calculateDistanceKm(
                        latitude,
                        longitude,
                        driver.getCurrentLocation().getLatitude(),
                        driver.getCurrentLocation().getLongitude()
                );

                // Exclude drivers exceeding requested radius
                if (distanceKm > effectiveRadius) {
                    continue;
                }
            }

            eligibleList.add(new EligibleDriverResponse(
                    driver.getId(),
                    driver.getUserId(),
                    driver.getLicenseNumber(),
                    driver.getPhoneNumber(),
                    driver.getAvailabilityStatus(),
                    driver.getServiceArea(),
                    driver.getCurrentLocation(),
                    distanceKm,
                    VehicleResponse.fromEntity(primaryVehicle)
            ));
        }

        // Sort by closest distance if distance was calculated
        if (latitude != null && longitude != null) {
            eligibleList.sort(Comparator.comparing(
                    d -> d.distanceKm() != null ? d.distanceKm() : Double.MAX_VALUE
            ));
        }

        return eligibleList;
    }
}
