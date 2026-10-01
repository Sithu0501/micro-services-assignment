/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ridelink.ridemanagement.exception.DriverNotAvailableException
 *  com.ridelink.ridemanagement.exception.InvalidRideRequestException
 *  com.ridelink.ridemanagement.exception.InvalidRideStateException
 *  com.ridelink.ridemanagement.model.Location
 *  com.ridelink.ridemanagement.model.Ride
 *  com.ridelink.ridemanagement.model.RideStatus
 *  com.ridelink.ridemanagement.repository.RideRepository
 *  com.ridelink.ridemanagement.security.UserPrincipal
 *  com.ridelink.ridemanagement.service.RideValidationService
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.BeforeEach
 *  org.junit.jupiter.api.DisplayName
 *  org.junit.jupiter.api.Test
 *  org.junit.jupiter.api.extension.ExtendWith
 *  org.mockito.ArgumentMatchers
 *  org.mockito.InjectMocks
 *  org.mockito.Mock
 *  org.mockito.Mockito
 *  org.mockito.junit.jupiter.MockitoExtension
 *  org.springframework.security.access.AccessDeniedException
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.exception.DriverNotAvailableException;
import com.ridelink.ridemanagement.exception.InvalidRideRequestException;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.model.Location;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.security.UserPrincipal;
import com.ridelink.ridemanagement.service.RideValidationService;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(value={MockitoExtension.class})
class RideValidationServiceTest {
    @Mock
    private RideRepository rideRepository;
    @InjectMocks
    private RideValidationService validationService;
    private Ride sampleRide;
    private UserPrincipal passengerPrincipal;
    private UserPrincipal otherPassengerPrincipal;
    private UserPrincipal driverPrincipal;
    private UserPrincipal adminPrincipal;

    RideValidationServiceTest() {
    }

    @BeforeEach
    void setUp() {
        this.sampleRide = new Ride("passenger123", new Location("SLIIT Malabe", Double.valueOf(6.9147), Double.valueOf(79.9729)), new Location("Fort", Double.valueOf(6.9344), Double.valueOf(79.8428)));
        this.sampleRide.setId("ride123");
        this.sampleRide.setDriverId("driver123");
        this.passengerPrincipal = new UserPrincipal("passenger123", "passenger@ridelink.com", "PASSENGER");
        this.otherPassengerPrincipal = new UserPrincipal("passenger999", "other@ridelink.com", "PASSENGER");
        this.driverPrincipal = new UserPrincipal("driver123", "driver@ridelink.com", "DRIVER");
        this.adminPrincipal = new UserPrincipal("admin123", "admin@ridelink.com", "ADMIN");
    }

    @Test
    @DisplayName(value="Passenger with active ride throws InvalidRideRequestException")
    void testValidatePassengerHasNoActiveRide_WhenActive_ThrowsException() {
        Mockito.when((Object)this.rideRepository.existsByPassengerIdAndStatusIn((String)ArgumentMatchers.eq((Object)"passenger123"), (Collection)ArgumentMatchers.any())).thenReturn((Object)true);
        Assertions.assertThrows(InvalidRideRequestException.class, () -> this.validationService.validatePassengerHasNoActiveRide("passenger123"));
    }

    @Test
    @DisplayName(value="Passenger with no active ride succeeds")
    void testValidatePassengerHasNoActiveRide_WhenNone_Succeeds() {
        Mockito.when((Object)this.rideRepository.existsByPassengerIdAndStatusIn((String)ArgumentMatchers.eq((Object)"passenger123"), (Collection)ArgumentMatchers.any())).thenReturn((Object)false);
        Assertions.assertDoesNotThrow(() -> this.validationService.validatePassengerHasNoActiveRide("passenger123"));
    }

    @Test
    @DisplayName(value="Driver with active ride throws DriverNotAvailableException")
    void testValidateDriverHasNoActiveRide_WhenActive_ThrowsException() {
        Mockito.when((Object)this.rideRepository.existsByDriverIdAndStatusIn((String)ArgumentMatchers.eq((Object)"driver123"), (Collection)ArgumentMatchers.any())).thenReturn((Object)true);
        Assertions.assertThrows(DriverNotAvailableException.class, () -> this.validationService.validateDriverHasNoActiveRide("driver123"));
    }

    @Test
    @DisplayName(value="Driver with no active ride succeeds")
    void testValidateDriverHasNoActiveRide_WhenNone_Succeeds() {
        Mockito.when((Object)this.rideRepository.existsByDriverIdAndStatusIn((String)ArgumentMatchers.eq((Object)"driver123"), (Collection)ArgumentMatchers.any())).thenReturn((Object)false);
        Assertions.assertDoesNotThrow(() -> this.validationService.validateDriverHasNoActiveRide("driver123"));
    }

    @Test
    @DisplayName(value="Valid status transitions succeed")
    void testValidateStatusTransition_Valid() {
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.REQUESTED, RideStatus.ASSIGNED));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.REQUESTED, RideStatus.CANCELLED));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.ASSIGNED, RideStatus.ACCEPTED));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.ASSIGNED, RideStatus.CANCELLED));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.ACCEPTED, RideStatus.IN_PROGRESS));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.ACCEPTED, RideStatus.CANCELLED));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateStatusTransition(RideStatus.IN_PROGRESS, RideStatus.COMPLETED));
    }

    @Test
    @DisplayName(value="Invalid status transitions throw InvalidRideStateException")
    void testValidateStatusTransition_Invalid() {
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateStatusTransition(RideStatus.COMPLETED, RideStatus.IN_PROGRESS));
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateStatusTransition(RideStatus.CANCELLED, RideStatus.COMPLETED));
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateStatusTransition(RideStatus.REQUESTED, RideStatus.COMPLETED));
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateStatusTransition(RideStatus.COMPLETED, RideStatus.CANCELLED));
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateStatusTransition(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
    }

    @Test
    @DisplayName(value="Ride is editable only in REQUESTED status")
    void testValidateRideIsEditable() {
        this.sampleRide.setStatus(RideStatus.REQUESTED);
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideIsEditable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateRideIsEditable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.COMPLETED);
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateRideIsEditable(this.sampleRide));
    }

    @Test
    @DisplayName(value="Ride is cancellable in REQUESTED, ASSIGNED, ACCEPTED only")
    void testValidateRideIsCancellable() {
        this.sampleRide.setStatus(RideStatus.REQUESTED);
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideIsCancellable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.ASSIGNED);
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideIsCancellable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.ACCEPTED);
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideIsCancellable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateRideIsCancellable(this.sampleRide));
        this.sampleRide.setStatus(RideStatus.COMPLETED);
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.validationService.validateRideIsCancellable(this.sampleRide));
    }

    @Test
    @DisplayName(value="Ownership validation allows owner and admin, denies others")
    void testValidateRideOwnership() {
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideOwnership(this.sampleRide, this.passengerPrincipal, "view"));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideOwnership(this.sampleRide, this.driverPrincipal, "view"));
        Assertions.assertDoesNotThrow(() -> this.validationService.validateRideOwnership(this.sampleRide, this.adminPrincipal, "view"));
        Assertions.assertThrows(AccessDeniedException.class, () -> this.validationService.validateRideOwnership(this.sampleRide, this.otherPassengerPrincipal, "view"));
    }

    @Test
    @DisplayName(value="Passenger history access allows own history or admin, denies other passengers")
    void testValidatePassengerHistoryAccess() {
        Assertions.assertDoesNotThrow(() -> this.validationService.validatePassengerHistoryAccess("passenger123", this.passengerPrincipal));
        Assertions.assertDoesNotThrow(() -> this.validationService.validatePassengerHistoryAccess("passenger123", this.adminPrincipal));
        Assertions.assertThrows(AccessDeniedException.class, () -> this.validationService.validatePassengerHistoryAccess("passenger123", this.otherPassengerPrincipal));
    }
}
