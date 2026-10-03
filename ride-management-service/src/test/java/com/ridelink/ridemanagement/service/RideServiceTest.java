/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ridelink.ridemanagement.client.AccountServiceClient
 *  com.ridelink.ridemanagement.client.dto.AccountUserDto
 *  com.ridelink.ridemanagement.dto.LocationDto
 *  com.ridelink.ridemanagement.dto.request.CancelRideRequest
 *  com.ridelink.ridemanagement.dto.request.CreateRideRequest
 *  com.ridelink.ridemanagement.dto.request.UpdateRideRequest
 *  com.ridelink.ridemanagement.dto.response.RideResponse
 *  com.ridelink.ridemanagement.dto.response.RideSummaryResponse
 *  com.ridelink.ridemanagement.exception.InvalidRideRequestException
 *  com.ridelink.ridemanagement.exception.RideNotFoundException
 *  com.ridelink.ridemanagement.model.Location
 *  com.ridelink.ridemanagement.model.Ride
 *  com.ridelink.ridemanagement.model.RideStatus
 *  com.ridelink.ridemanagement.repository.RideRepository
 *  com.ridelink.ridemanagement.security.UserPrincipal
 *  com.ridelink.ridemanagement.service.RideAssignmentService
 *  com.ridelink.ridemanagement.service.RideService
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
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageImpl
 *  org.springframework.data.domain.Pageable
 *  org.springframework.security.access.AccessDeniedException
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.AccountServiceClient;
import com.ridelink.ridemanagement.client.FareServiceClient;
import com.ridelink.ridemanagement.client.dto.AccountUserDto;
import com.ridelink.ridemanagement.dto.LocationDto;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideRequest;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.dto.response.RideSummaryResponse;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import com.ridelink.ridemanagement.exception.InvalidRideRequestException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.model.Location;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.security.UserPrincipal;
import com.ridelink.ridemanagement.service.RideAssignmentService;
import com.ridelink.ridemanagement.service.RideService;
import com.ridelink.ridemanagement.service.RideValidationService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(value={MockitoExtension.class})
class RideServiceTest {
    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideValidationService rideValidationService;
    @Mock
    private RideAssignmentService rideAssignmentService;
    @Mock
    private AccountServiceClient accountServiceClient;
    @Mock
    private FareServiceClient fareServiceClient;
    @InjectMocks
    private RideService rideService;
    private UserPrincipal passengerPrincipal;
    private UserPrincipal driverPrincipal;
    private UserPrincipal adminPrincipal;
    private Ride sampleRide;
    private AccountUserDto activeAccount;

    RideServiceTest() {
    }

    @BeforeEach
    void setUp() {
        this.passengerPrincipal = new UserPrincipal("passenger123", "passenger@ridelink.com", "PASSENGER");
        this.driverPrincipal = new UserPrincipal("driver123", "driver@ridelink.com", "DRIVER");
        this.adminPrincipal = new UserPrincipal("admin123", "admin@ridelink.com", "ADMIN");
        this.sampleRide = new Ride("passenger123", new Location("SLIIT Malabe", Double.valueOf(6.9147), Double.valueOf(79.9729)), new Location("Fort", Double.valueOf(6.9344), Double.valueOf(79.8428)));
        this.sampleRide.setId("ride123");
        this.sampleRide.setDriverId("driver123");
        this.sampleRide.setStatus(RideStatus.REQUESTED);
        this.activeAccount = new AccountUserDto("passenger123", "Kasun Perera", "kasun@example.com", "+94771234567", "PASSENGER", "ACTIVE", Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("Create ride succeeds for active passenger")
    void testCreateRide_Success() {
        CreateRideRequest request = new CreateRideRequest(null, new LocationDto("SLIIT Malabe", 6.9147, 79.9729), new LocationDto("Fort", 6.9344, 79.8428));
        Mockito.when(this.accountServiceClient.getUserById(ArgumentMatchers.eq("passenger123"), ArgumentMatchers.any())).thenReturn(this.activeAccount);
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId("rideGenerated123");
            return r;
        });
        RideResponse response = this.rideService.createRide(request, this.passengerPrincipal, "mock-token");
        Assertions.assertNotNull(response);
        Assertions.assertEquals("rideGenerated123", response.id());
        Assertions.assertEquals("passenger123", response.passengerId());
        Assertions.assertEquals(RideStatus.REQUESTED, response.status());
        Mockito.verify(this.rideValidationService).validatePassengerHasNoActiveRide("passenger123");
    }

    @Test
    @DisplayName("Create ride fails if passenger account is inactive")
    void testCreateRide_InactivePassenger_ThrowsException() {
        CreateRideRequest request = new CreateRideRequest(null, new LocationDto("SLIIT Malabe", 6.9147, 79.9729), new LocationDto("Fort", 6.9344, 79.8428));
        AccountUserDto inactiveAccount = new AccountUserDto("passenger123", "Kasun Perera", "kasun@example.com", "+94771234567", "PASSENGER", "SUSPENDED", Instant.now(), Instant.now());
        Mockito.when(this.accountServiceClient.getUserById(ArgumentMatchers.eq("passenger123"), ArgumentMatchers.any())).thenReturn(inactiveAccount);
        Assertions.assertThrows(InvalidRideRequestException.class, () -> this.rideService.createRide(request, this.passengerPrincipal, "mock-token"));
    }

    @Test
    @DisplayName("Get ride by ID succeeds when ride exists")
    void testGetRideById_Success() {
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        RideResponse response = this.rideService.getRideById("ride123", this.passengerPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("ride123", response.id());
        Mockito.verify(this.rideValidationService).validateRideOwnership(this.sampleRide, this.passengerPrincipal, "view");
    }

    @Test
    @DisplayName("Get ride by ID throws RideNotFoundException when nonexistent")
    void testGetRideById_NotFound() {
        Mockito.when(this.rideRepository.findById("unknownRide")).thenReturn(Optional.empty());
        Assertions.assertThrows(RideNotFoundException.class, () -> this.rideService.getRideById("unknownRide", this.passengerPrincipal));
    }

    @Test
    @DisplayName("Update ride updates locations when in REQUESTED status")
    void testUpdateRide_Success() {
        UpdateRideRequest request = new UpdateRideRequest(new LocationDto("New Pickup", 6.92, 79.98), new LocationDto("New Destination", 6.94, 79.85));
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RideResponse response = this.rideService.updateRide("ride123", request, this.passengerPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("New Pickup", response.pickupLocation().address());
        Assertions.assertEquals("New Destination", response.destinationLocation().address());
        Mockito.verify(this.rideValidationService).validateRideIsEditable(this.sampleRide);
    }

    @Test
    @DisplayName("Delete ride succeeds for ADMIN role")
    void testDeleteRide_AdminSuccess() {
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Assertions.assertDoesNotThrow(() -> this.rideService.deleteRide("ride123", this.adminPrincipal));
        Mockito.verify(this.rideRepository).delete(this.sampleRide);
    }

    @Test
    @DisplayName("Delete ride throws AccessDeniedException for non-admin")
    void testDeleteRide_NonAdmin_ThrowsAccessDenied() {
        Assertions.assertThrows(AccessDeniedException.class, () -> this.rideService.deleteRide("ride123", this.passengerPrincipal));
    }

    @Test
    @DisplayName("Driver accepts assigned ride successfully")
    void testAcceptRide_Success() {
        this.sampleRide.setStatus(RideStatus.ASSIGNED);
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RideResponse response = this.rideService.acceptRide("ride123", this.driverPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(RideStatus.ACCEPTED, response.status());
        Assertions.assertNotNull(response.acceptedAt());
    }

    @Test
    @DisplayName("Driver starts accepted ride successfully")
    void testStartRide_Success() {
        this.sampleRide.setStatus(RideStatus.ACCEPTED);
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RideResponse response = this.rideService.startRide("ride123", this.driverPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(RideStatus.IN_PROGRESS, response.status());
        Assertions.assertNotNull(response.startedAt());
    }

    @Test
    @DisplayName("Driver completes in-progress ride successfully")
    void testCompleteRide_Success() {
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RideResponse response = this.rideService.completeRide("ride123", this.driverPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(RideStatus.COMPLETED, response.status());
        Assertions.assertNotNull(response.completedAt());
    }

    @Test
    @DisplayName("Completing a ride requests the final fare from Fare & Payment using the trip distance")
    void testCompleteRide_RequestsFinalFare() {
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        this.rideService.completeRide("ride123", this.driverPrincipal);
        // SLIIT Malabe -> Fort is roughly 15 km as the crow flies.
        Mockito.verify(this.fareServiceClient).createFinalFare(ArgumentMatchers.eq("ride123"), ArgumentMatchers.doubleThat(km -> km > 10.0 && km < 20.0));
    }

    @Test
    @DisplayName("Ride is still completed when Fare & Payment is unavailable")
    void testCompleteRide_FareServiceDown_StillCompletes() {
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.doThrow(new ExternalServiceException("Fare & Payment Service is currently unavailable")).when(this.fareServiceClient).createFinalFare(ArgumentMatchers.anyString(), ArgumentMatchers.anyDouble());
        RideResponse response = this.rideService.completeRide("ride123", this.driverPrincipal);
        Assertions.assertEquals(RideStatus.COMPLETED, response.status());
    }

    @Test
    @DisplayName("No final fare is requested when coordinates are missing")
    void testCompleteRide_MissingCoordinates_SkipsFare() {
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        this.sampleRide.setPickupLocation(new Location("Unknown pickup", null, null));
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        this.rideService.completeRide("ride123", this.driverPrincipal);
        Mockito.verifyNoInteractions(this.fareServiceClient);
    }

    @Test
    @DisplayName("Cancel ride sets CANCELLED status, timestamp, and reason")
    void testCancelRide_Success() {
        CancelRideRequest request = new CancelRideRequest("Meeting postponed");
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RideResponse response = this.rideService.cancelRide("ride123", request, this.passengerPrincipal);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(RideStatus.CANCELLED, response.status());
        Assertions.assertNotNull(response.cancelledAt());
        Assertions.assertEquals("Meeting postponed", response.cancellationReason());
    }

    @Test
    @DisplayName("Passenger ride history queries passenger repository method")
    void testGetPassengerRideHistory() {
        PageImpl<Ride> page = new PageImpl<>(List.of(this.sampleRide));
        Mockito.when(this.rideRepository.findByPassengerId(ArgumentMatchers.eq("passenger123"), ArgumentMatchers.any(Pageable.class))).thenReturn(page);
        Page<RideSummaryResponse> history = this.rideService.getPassengerRideHistory("passenger123", 0, 20, this.passengerPrincipal);
        Assertions.assertNotNull(history);
        Assertions.assertEquals(1L, history.getTotalElements());
        Assertions.assertEquals("ride123", history.getContent().get(0).id());
        Mockito.verify(this.rideValidationService).validatePassengerHistoryAccess("passenger123", this.passengerPrincipal);
    }
}
