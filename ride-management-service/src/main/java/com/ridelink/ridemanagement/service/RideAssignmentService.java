/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.DriverServiceClient;
import com.ridelink.ridemanagement.client.dto.DriverProfileDto;
import com.ridelink.ridemanagement.client.dto.EligibleDriverDto;
import com.ridelink.ridemanagement.client.dto.VehicleDto;
import com.ridelink.ridemanagement.dto.request.AssignDriverRequest;
import com.ridelink.ridemanagement.exception.DriverNotAvailableException;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.service.RideValidationService;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RideAssignmentService {
    private static final Logger log = LoggerFactory.getLogger(RideAssignmentService.class);
    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final RideValidationService rideValidationService;

    public RideAssignmentService(RideRepository rideRepository, DriverServiceClient driverServiceClient, RideValidationService rideValidationService) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.rideValidationService = rideValidationService;
    }

    public Ride assignDriver(Ride ride, AssignDriverRequest request, String bearerToken) {
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStateException("Cannot assign driver to ride in status '" + String.valueOf((Object)ride.getStatus()) + "'. Driver assignment is only permitted for rides in REQUESTED status.");
        }
        String driverId = request.driverId();
        DriverProfileDto driverProfile = this.driverServiceClient.getDriverById(driverId, bearerToken);
        if (!driverProfile.isAvailable()) {
            throw new DriverNotAvailableException("Driver " + driverId + " is currently in '" + driverProfile.availabilityStatus() + "' status and cannot accept assignments.");
        }
        this.rideValidationService.validateDriverHasNoActiveRide(driverId);
        String vehicleId = request.vehicleId();
        if (vehicleId != null && !vehicleId.isBlank()) {
            VehicleDto vehicle = this.driverServiceClient.getVehicleById(vehicleId, bearerToken);
            vehicleId = vehicle.id();
        }
        ride.setDriverId(driverId);
        ride.setVehicleId(vehicleId);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        log.info("Driver {} successfully assigned to ride {}", (Object)driverId, (Object)ride.getId());
        return (Ride)this.rideRepository.save(ride);
    }

    public Ride autoAssignDriver(Ride ride, String bearerToken) {
        Double pickupLng;
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStateException("Cannot automatically assign driver to ride in status '" + String.valueOf((Object)ride.getStatus()) + "'.");
        }
        Double pickupLat = ride.getPickupLocation() != null ? ride.getPickupLocation().getLatitude() : null;
        List<EligibleDriverDto> eligibleDrivers = this.driverServiceClient.getEligibleDrivers(pickupLat, pickupLng = ride.getPickupLocation() != null ? ride.getPickupLocation().getLongitude() : null, 10.0, null, bearerToken);
        if (eligibleDrivers.isEmpty()) {
            throw new DriverNotAvailableException("No eligible available drivers found within 10.0 km radius of pickup location.");
        }
        EligibleDriverDto selectedDriver = null;
        for (EligibleDriverDto candidate : eligibleDrivers) {
            try {
                this.rideValidationService.validateDriverHasNoActiveRide(candidate.driverId());
                selectedDriver = candidate;
                break;
            }
            catch (DriverNotAvailableException e) {
                log.debug("Candidate driver {} already has active ride, skipping", (Object)candidate.driverId());
            }
        }
        if (selectedDriver == null) {
            throw new DriverNotAvailableException("All nearby drivers within dispatch radius are currently engaged on active rides.");
        }
        String resolvedVehicleId = selectedDriver.vehicle() != null ? selectedDriver.vehicle().id() : null;
        ride.setDriverId(selectedDriver.driverId());
        ride.setVehicleId(resolvedVehicleId);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(Instant.now());
        log.info("Driver {} automatically assigned to ride {} (distance: {} km)", new Object[]{selectedDriver.driverId(), ride.getId(), selectedDriver.distanceKm()});
        return (Ride)this.rideRepository.save(ride);
    }
}

