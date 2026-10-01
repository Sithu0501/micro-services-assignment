/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.domain.Sort
 *  org.springframework.data.domain.Sort$Direction
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.stereotype.Service
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.AccountServiceClient;
import com.ridelink.ridemanagement.client.dto.AccountUserDto;
import com.ridelink.ridemanagement.dto.request.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideStatusRequest;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.dto.response.RideSummaryResponse;
import com.ridelink.ridemanagement.exception.InvalidRideRequestException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.security.UserPrincipal;
import com.ridelink.ridemanagement.service.RideAssignmentService;
import com.ridelink.ridemanagement.service.RideValidationService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class RideService {
    private static final Logger log = LoggerFactory.getLogger(RideService.class);
    private final RideRepository rideRepository;
    private final RideValidationService rideValidationService;
    private final RideAssignmentService rideAssignmentService;
    private final AccountServiceClient accountServiceClient;

    public RideService(RideRepository rideRepository, RideValidationService rideValidationService, RideAssignmentService rideAssignmentService, AccountServiceClient accountServiceClient) {
        this.rideRepository = rideRepository;
        this.rideValidationService = rideValidationService;
        this.rideAssignmentService = rideAssignmentService;
        this.accountServiceClient = accountServiceClient;
    }

    public RideResponse createRide(CreateRideRequest request, UserPrincipal principal, String bearerToken) {
        String passengerId = principal.isAdmin() && request.passengerId() != null && !request.passengerId().isBlank() ? request.passengerId().trim() : principal.getUserId();
        this.rideValidationService.validatePassengerHasNoActiveRide(passengerId);
        AccountUserDto passengerAccount = this.accountServiceClient.getUserById(passengerId, bearerToken);
        if (!passengerAccount.isActive()) {
            throw new InvalidRideRequestException("Passenger account " + passengerId + " is not active");
        }
        Ride ride = new Ride(passengerId, request.pickupLocation().toEntity(), request.destinationLocation().toEntity());
        Ride savedRide = (Ride)this.rideRepository.save(ride);
        log.info("Ride requested successfully: ID={}, Passenger={}", (Object)savedRide.getId(), (Object)passengerId);
        return RideResponse.fromEntity(savedRide);
    }

    public RideResponse getRideById(String rideId, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.rideValidationService.validateRideOwnership(ride, principal, "view");
        return RideResponse.fromEntity(ride);
    }

    public Page<RideSummaryResponse> listRides(RideStatus status, String passengerId, String driverId, int page, int size, String sortDirection, UserPrincipal principal) {
        if (principal.isPassenger()) {
            passengerId = principal.getUserId();
        } else if (principal.isDriver()) {
            driverId = principal.getUserId();
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of((int)Math.max(0, page), (int)Math.min(Math.max(1, size), 100), (Sort)Sort.by((Sort.Direction)direction, (String[])new String[]{"requestedAt"}));
        Page<Ride> rides = passengerId != null && driverId != null && status != null ? this.rideRepository.findByPassengerIdAndDriverIdAndStatus(passengerId, driverId, status, (Pageable)pageable) : (passengerId != null && driverId != null ? this.rideRepository.findByPassengerIdAndDriverId(passengerId, driverId, (Pageable)pageable) : (passengerId != null && status != null ? this.rideRepository.findByPassengerIdAndStatus(passengerId, status, (Pageable)pageable) : (driverId != null && status != null ? this.rideRepository.findByDriverIdAndStatus(driverId, status, (Pageable)pageable) : (passengerId != null ? this.rideRepository.findByPassengerId(passengerId, (Pageable)pageable) : (driverId != null ? this.rideRepository.findByDriverId(driverId, (Pageable)pageable) : (status != null ? this.rideRepository.findByStatus(status, (Pageable)pageable) : this.rideRepository.findAll((Pageable)pageable)))))));
        return rides.map(RideSummaryResponse::fromEntity);
    }

    public RideResponse updateRide(String rideId, UpdateRideRequest request, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.rideValidationService.validateRideOwnership(ride, principal, "update");
        this.rideValidationService.validateRideIsEditable(ride);
        ride.setPickupLocation(request.pickupLocation().toEntity());
        ride.setDestinationLocation(request.destinationLocation().toEntity());
        Ride updatedRide = (Ride)this.rideRepository.save(ride);
        log.info("Ride {} updated by {}", (Object)rideId, (Object)principal.getUserId());
        return RideResponse.fromEntity(updatedRide);
    }

    public void deleteRide(String rideId, UserPrincipal principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Only administrators are permitted to delete ride records");
        }
        Ride ride = this.findRideOrThrow(rideId);
        this.rideRepository.delete(ride);
        log.info("Ride {} deleted by administrator {}", (Object)rideId, (Object)principal.getUserId());
    }

    public RideResponse assignDriver(String rideId, AssignDriverRequest request, UserPrincipal principal, String bearerToken) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Only administrators or dispatch coordinators may manually assign drivers");
        }
        Ride ride = this.findRideOrThrow(rideId);
        Ride updated = this.rideAssignmentService.assignDriver(ride, request, bearerToken);
        return RideResponse.fromEntity(updated);
    }

    public RideResponse autoAssignDriver(String rideId, UserPrincipal principal, String bearerToken) {
        Ride ride = this.findRideOrThrow(rideId);
        this.rideValidationService.validateRideOwnership(ride, principal, "request driver assignment for");
        Ride updated = this.rideAssignmentService.autoAssignDriver(ride, bearerToken);
        return RideResponse.fromEntity(updated);
    }

    public RideResponse acceptRide(String rideId, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.validateDriverOrAdmin(ride, principal, "accept");
        this.rideValidationService.validateStatusTransition(ride.getStatus(), RideStatus.ACCEPTED);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());
        Ride saved = (Ride)this.rideRepository.save(ride);
        log.info("Ride {} accepted by driver {}", (Object)rideId, (Object)principal.getUserId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse startRide(String rideId, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.validateDriverOrAdmin(ride, principal, "start");
        this.rideValidationService.validateStatusTransition(ride.getStatus(), RideStatus.IN_PROGRESS);
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());
        Ride saved = (Ride)this.rideRepository.save(ride);
        log.info("Ride {} started by driver {}", (Object)rideId, (Object)principal.getUserId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse completeRide(String rideId, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.validateDriverOrAdmin(ride, principal, "complete");
        this.rideValidationService.validateStatusTransition(ride.getStatus(), RideStatus.COMPLETED);
        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());
        Ride saved = (Ride)this.rideRepository.save(ride);
        log.info("Ride {} completed by driver {}", (Object)rideId, (Object)principal.getUserId());
        return RideResponse.fromEntity(saved);
    }

    public RideResponse updateRideStatus(String rideId, UpdateRideStatusRequest request, UserPrincipal principal) {
        RideStatus targetStatus = request.status();
        return switch (targetStatus) {
            case RideStatus.ACCEPTED -> this.acceptRide(rideId, principal);
            case RideStatus.IN_PROGRESS -> this.startRide(rideId, principal);
            case RideStatus.COMPLETED -> this.completeRide(rideId, principal);
            case RideStatus.CANCELLED -> this.cancelRide(rideId, new CancelRideRequest(request.note() != null ? request.note() : "Cancelled via status update"), principal);
            default -> throw new InvalidRideRequestException("Direct status transition to " + String.valueOf((Object)targetStatus) + " is not permitted via this endpoint");
        };
    }

    public RideResponse cancelRide(String rideId, CancelRideRequest request, UserPrincipal principal) {
        Ride ride = this.findRideOrThrow(rideId);
        this.rideValidationService.validateRideOwnership(ride, principal, "cancel");
        this.rideValidationService.validateRideIsCancellable(ride);
        this.rideValidationService.validateStatusTransition(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(Instant.now());
        ride.setCancellationReason(request.reason());
        Ride saved = (Ride)this.rideRepository.save(ride);
        log.info("Ride {} cancelled by {}. Reason: {}", new Object[]{rideId, principal.getUserId(), request.reason()});
        return RideResponse.fromEntity(saved);
    }

    public Page<RideSummaryResponse> getPassengerRideHistory(String passengerId, int page, int size, UserPrincipal principal) {
        this.rideValidationService.validatePassengerHistoryAccess(passengerId, principal);
        PageRequest pageable = PageRequest.of((int)Math.max(0, page), (int)Math.min(Math.max(1, size), 100), (Sort)Sort.by((Sort.Direction)Sort.Direction.DESC, (String[])new String[]{"requestedAt"}));
        return this.rideRepository.findByPassengerId(passengerId, (Pageable)pageable).map(RideSummaryResponse::fromEntity);
    }

    public Page<RideSummaryResponse> getDriverRideHistory(String driverId, int page, int size, UserPrincipal principal) {
        this.rideValidationService.validateDriverHistoryAccess(driverId, principal);
        PageRequest pageable = PageRequest.of((int)Math.max(0, page), (int)Math.min(Math.max(1, size), 100), (Sort)Sort.by((Sort.Direction)Sort.Direction.DESC, (String[])new String[]{"requestedAt"}));
        return this.rideRepository.findByDriverId(driverId, (Pageable)pageable).map(RideSummaryResponse::fromEntity);
    }

    private Ride findRideOrThrow(String rideId) {
        return (Ride)this.rideRepository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
    }

    private void validateDriverOrAdmin(Ride ride, UserPrincipal principal, String action) {
        if (principal.isAdmin()) {
            return;
        }
        if (!principal.isDriver() || !principal.getUserId().equals(ride.getDriverId())) {
            throw new AccessDeniedException("Only the assigned driver is authorized to " + action + " this ride");
        }
    }
}

