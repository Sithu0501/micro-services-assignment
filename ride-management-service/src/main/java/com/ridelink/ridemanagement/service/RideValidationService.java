/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.stereotype.Service
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.exception.DriverNotAvailableException;
import com.ridelink.ridemanagement.exception.InvalidRideRequestException;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.security.UserPrincipal;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class RideValidationService {
    private static final Logger log = LoggerFactory.getLogger(RideValidationService.class);
    private static final Set<RideStatus> ACTIVE_STATUSES = Set.of(RideStatus.REQUESTED, RideStatus.ASSIGNED, RideStatus.ACCEPTED, RideStatus.IN_PROGRESS);
    private final RideRepository rideRepository;

    public RideValidationService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public void validatePassengerHasNoActiveRide(String passengerId) {
        boolean hasActive = this.rideRepository.existsByPassengerIdAndStatusIn(passengerId, ACTIVE_STATUSES);
        if (hasActive) {
            log.warn("Active ride conflict for passenger: {}", (Object)passengerId);
            throw new InvalidRideRequestException("Passenger " + passengerId + " already has an active ride in progress. Please complete or cancel the existing ride before requesting a new one.");
        }
    }

    public void validateDriverHasNoActiveRide(String driverId) {
        boolean hasActive = this.rideRepository.existsByDriverIdAndStatusIn(driverId, ACTIVE_STATUSES);
        if (hasActive) {
            log.warn("Active ride conflict for driver: {}", (Object)driverId);
            throw new DriverNotAvailableException("Driver " + driverId + " is currently assigned to another active ride and cannot accept new assignments.");
        }
    }

    public void validateStatusTransition(RideStatus currentStatus, RideStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidRideStateException("Current and target ride status must not be null");
        }
        if (!currentStatus.canTransitionTo(targetStatus)) {
            log.warn("Illegal lifecycle transition attempted: {} -> {}", (Object)currentStatus, (Object)targetStatus);
            throw new InvalidRideStateException(String.format("Cannot transition ride status from '%s' to '%s'. Allowed transitions from '%s': %s", new Object[]{currentStatus, targetStatus, currentStatus, currentStatus.allowedNextStates()}));
        }
    }

    public void validateRideIsEditable(Ride ride) {
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStateException("Ride cannot be modified in status '" + String.valueOf((Object)ride.getStatus()) + "'. Location updates are permitted only while in REQUESTED status.");
        }
    }

    public void validateRideIsCancellable(Ride ride) {
        if (!ride.getStatus().isCancellable()) {
            throw new InvalidRideStateException("Ride cannot be cancelled in status '" + String.valueOf((Object)ride.getStatus()) + "'. Cancellation is only allowed for REQUESTED, ASSIGNED, or ACCEPTED rides.");
        }
    }

    public void validateRideOwnership(Ride ride, UserPrincipal principal, String action) {
        boolean isAssignedDriver;
        if (principal == null) {
            throw new AccessDeniedException("User must be authenticated to perform " + action);
        }
        if (principal.isAdmin()) {
            return;
        }
        String callerId = principal.getUserId();
        if (principal.isPassenger()) {
            if (!callerId.equals(ride.getPassengerId())) {
                log.warn("Passenger {} attempted unauthorized {} on ride {} belonging to {}", new Object[]{callerId, action, ride.getId(), ride.getPassengerId()});
                throw new AccessDeniedException("You are not authorized to " + action + " another passenger's ride");
            }
        } else if (principal.isDriver() && !(isAssignedDriver = callerId.equals(ride.getDriverId()))) {
            log.warn("Driver {} attempted unauthorized {} on ride {} assigned to {}", new Object[]{callerId, action, ride.getId(), ride.getDriverId()});
            throw new AccessDeniedException("You are not authorized to " + action + " a ride not assigned to you");
        }
    }

    public void validatePassengerHistoryAccess(String requestedPassengerId, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (principal.isAdmin()) {
            return;
        }
        if (!principal.getUserId().equals(requestedPassengerId)) {
            throw new AccessDeniedException("You are not authorized to view another passenger's ride history");
        }
    }

    public void validateDriverHistoryAccess(String requestedDriverId, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (principal.isAdmin()) {
            return;
        }
        if (!principal.getUserId().equals(requestedDriverId)) {
            throw new AccessDeniedException("You are not authorized to view another driver's ride history");
        }
    }
}

