/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ridelink.ridemanagement.client.DriverServiceClient
 *  com.ridelink.ridemanagement.client.dto.DriverProfileDto
 *  com.ridelink.ridemanagement.client.dto.EligibleDriverDto
 *  com.ridelink.ridemanagement.client.dto.VehicleDto
 *  com.ridelink.ridemanagement.dto.request.AssignDriverRequest
 *  com.ridelink.ridemanagement.exception.DriverNotAvailableException
 *  com.ridelink.ridemanagement.exception.InvalidRideStateException
 *  com.ridelink.ridemanagement.model.Location
 *  com.ridelink.ridemanagement.model.Ride
 *  com.ridelink.ridemanagement.model.RideStatus
 *  com.ridelink.ridemanagement.repository.RideRepository
 *  com.ridelink.ridemanagement.service.RideAssignmentService
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
 */
package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.DriverServiceClient;
import com.ridelink.ridemanagement.client.dto.DriverProfileDto;
import com.ridelink.ridemanagement.client.dto.EligibleDriverDto;
import com.ridelink.ridemanagement.client.dto.VehicleDto;
import com.ridelink.ridemanagement.dto.request.AssignDriverRequest;
import com.ridelink.ridemanagement.exception.DriverNotAvailableException;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.model.Location;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.service.RideAssignmentService;
import com.ridelink.ridemanagement.service.RideValidationService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
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

@ExtendWith(value={MockitoExtension.class})
class RideAssignmentServiceTest {
    @Mock
    private RideRepository rideRepository;
    @Mock
    private DriverServiceClient driverServiceClient;
    @Mock
    private RideValidationService rideValidationService;
    @InjectMocks
    private RideAssignmentService rideAssignmentService;
    private Ride sampleRide;
    private DriverProfileDto availableDriverProfile;
    private DriverProfileDto busyDriverProfile;

    RideAssignmentServiceTest() {
    }

    @BeforeEach
    void setUp() {
        this.sampleRide = new Ride("passenger123", new Location("SLIIT", Double.valueOf(6.9147), Double.valueOf(79.9729)), new Location("Fort", Double.valueOf(6.9344), Double.valueOf(79.8428)));
        this.sampleRide.setId("ride123");
        this.sampleRide.setStatus(RideStatus.REQUESTED);
        this.availableDriverProfile = new DriverProfileDto("driver123", "user123", "B1234567", "2028-12-31", "+94771234567", "AVAILABLE", "Colombo", new Location("Malabe", Double.valueOf(6.914), Double.valueOf(79.972)), Instant.now(), Instant.now());
        this.busyDriverProfile = new DriverProfileDto("driverBusy", "userBusy", "B7654321", "2028-12-31", "+94777654321", "ON_TRIP", "Colombo", new Location("Malabe", Double.valueOf(6.914), Double.valueOf(79.972)), Instant.now(), Instant.now());
    }

    @Test
    @DisplayName(value="Manual assignment succeeds for available driver")
    void testAssignDriver_Success() {
        AssignDriverRequest request = new AssignDriverRequest("driver123", "vehicle123");
        VehicleDto vehicle = new VehicleDto("vehicle123", "driver123", "Toyota", "Prius", Integer.valueOf(2020), "WP-CAD-1234", "CAR", "White");
        Mockito.when((Object)this.driverServiceClient.getDriverById((String)ArgumentMatchers.eq((Object)"driver123"), (String)ArgumentMatchers.any())).thenReturn((Object)this.availableDriverProfile);
        Mockito.when((Object)this.driverServiceClient.getVehicleById((String)ArgumentMatchers.eq((Object)"vehicle123"), (String)ArgumentMatchers.any())).thenReturn((Object)vehicle);
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Ride result = this.rideAssignmentService.assignDriver(this.sampleRide, request, "mock-token");
        Assertions.assertNotNull((Object)result);
        Assertions.assertEquals((Object)"driver123", (Object)result.getDriverId());
        Assertions.assertEquals((Object)"vehicle123", (Object)result.getVehicleId());
        Assertions.assertEquals((Object)RideStatus.ASSIGNED, (Object)result.getStatus());
        Assertions.assertNotNull((Object)result.getAssignedAt());
        ((RideValidationService)Mockito.verify((Object)this.rideValidationService)).validateDriverHasNoActiveRide("driver123");
    }

    @Test
    @DisplayName(value="Manual assignment fails if driver is not in AVAILABLE status")
    void testAssignDriver_DriverNotAvailable_ThrowsException() {
        AssignDriverRequest request = new AssignDriverRequest("driverBusy", null);
        Mockito.when((Object)this.driverServiceClient.getDriverById((String)ArgumentMatchers.eq((Object)"driverBusy"), (String)ArgumentMatchers.any())).thenReturn((Object)this.busyDriverProfile);
        Assertions.assertThrows(DriverNotAvailableException.class, () -> this.rideAssignmentService.assignDriver(this.sampleRide, request, "mock-token"));
    }

    @Test
    @DisplayName(value="Manual assignment fails if ride is not in REQUESTED status")
    void testAssignDriver_InvalidRideStatus_ThrowsException() {
        this.sampleRide.setStatus(RideStatus.IN_PROGRESS);
        AssignDriverRequest request = new AssignDriverRequest("driver123", null);
        Assertions.assertThrows(InvalidRideStateException.class, () -> this.rideAssignmentService.assignDriver(this.sampleRide, request, "mock-token"));
    }

    @Test
    @DisplayName(value="Auto-assignment finds and dispatches closest eligible driver")
    void testAutoAssignDriver_Success() {
        VehicleDto vehicle = new VehicleDto("vehicle123", "driver123", "Toyota", "Prius", Integer.valueOf(2020), "WP-CAD-1234", "CAR", "White");
        EligibleDriverDto eligibleDriver = new EligibleDriverDto("driver123", "user123", "B1234567", "+94771234567", "AVAILABLE", "Colombo", new Location("SLIIT", Double.valueOf(6.915), Double.valueOf(79.973)), Double.valueOf(0.5), vehicle);
        Mockito.when((Object)this.driverServiceClient.getEligibleDrivers((Double)ArgumentMatchers.any(), (Double)ArgumentMatchers.any(), (Double)ArgumentMatchers.any(), (String)ArgumentMatchers.any(), (String)ArgumentMatchers.any())).thenReturn(List.of(eligibleDriver));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Ride result = this.rideAssignmentService.autoAssignDriver(this.sampleRide, "mock-token");
        Assertions.assertNotNull((Object)result);
        Assertions.assertEquals((Object)"driver123", (Object)result.getDriverId());
        Assertions.assertEquals((Object)"vehicle123", (Object)result.getVehicleId());
        Assertions.assertEquals((Object)RideStatus.ASSIGNED, (Object)result.getStatus());
        Assertions.assertNotNull((Object)result.getAssignedAt());
    }

    @Test
    @DisplayName(value="Auto-assignment throws exception when no eligible drivers found")
    void testAutoAssignDriver_NoDrivers_ThrowsException() {
        Mockito.when((Object)this.driverServiceClient.getEligibleDrivers((Double)ArgumentMatchers.any(), (Double)ArgumentMatchers.any(), (Double)ArgumentMatchers.any(), (String)ArgumentMatchers.any(), (String)ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        Assertions.assertThrows(DriverNotAvailableException.class, () -> this.rideAssignmentService.autoAssignDriver(this.sampleRide, "mock-token"));
    }
}
