package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicleservice.dto.response.DriverResponse;
import com.ridelink.drivervehicleservice.dto.response.EligibleDriverResponse;
import com.ridelink.drivervehicleservice.exception.BadRequestException;
import com.ridelink.drivervehicleservice.exception.DuplicateResourceException;
import com.ridelink.drivervehicleservice.exception.ResourceNotFoundException;
import com.ridelink.drivervehicleservice.model.*;
import com.ridelink.drivervehicleservice.repository.DriverRepository;
import com.ridelink.drivervehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverService Unit Tests")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver testDriver;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testDriver = new Driver(
                "drv-001",
                "usr-100",
                "B1234567",
                "2028-12-31",
                "+94771234567",
                AvailabilityStatus.UNAVAILABLE,
                "Colombo",
                new Location(6.9271, 79.8612)
        );

        testVehicle = new Vehicle(
                "veh-001",
                "drv-001",
                "CAB-1234",
                "Toyota",
                "Prius",
                2022,
                "White",
                VehicleType.SEDAN,
                4
        );
    }

    @Test
    @DisplayName("Should successfully create a driver profile")
    void createDriverProfile_Success() {
        CreateDriverRequest request = new CreateDriverRequest("B1234567", "2028-12-31", "+94771234567", "Colombo");

        when(driverRepository.existsByUserId("usr-100")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver d = invocation.getArgument(0);
            d.setId("drv-001");
            return d;
        });

        DriverResponse response = driverService.createDriverProfile("usr-100", request);

        assertNotNull(response);
        assertEquals("usr-100", response.userId());
        assertEquals("B1234567", response.licenseNumber());
        assertEquals(AvailabilityStatus.UNAVAILABLE, response.availabilityStatus());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when profile already exists for userId")
    void createDriverProfile_DuplicateUserId_Throws() {
        CreateDriverRequest request = new CreateDriverRequest("B1234567", "2028-12-31", "+94771234567", "Colombo");

        when(driverRepository.existsByUserId("usr-100")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.createDriverProfile("usr-100", request));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when license number is already registered")
    void createDriverProfile_DuplicateLicense_Throws() {
        CreateDriverRequest request = new CreateDriverRequest("B1234567", "2028-12-31", "+94771234567", "Colombo");

        when(driverRepository.existsByUserId("usr-100")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> driverService.createDriverProfile("usr-100", request));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should retrieve driver profile by userId")
    void getDriverProfileByUserId_Success() {
        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));

        DriverResponse response = driverService.getDriverProfileByUserId("usr-100");

        assertNotNull(response);
        assertEquals("drv-001", response.id());
        assertEquals("B1234567", response.licenseNumber());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when profile not found by userId")
    void getDriverProfileByUserId_NotFound_Throws() {
        when(driverRepository.findByUserId("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverProfileByUserId("unknown"));
    }

    @Test
    @DisplayName("Should update driver profile details")
    void updateDriverProfile_Success() {
        UpdateDriverRequest request = new UpdateDriverRequest("2029-05-15", "+94779876543", "Kandy");

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateDriverProfile("usr-100", request);

        assertEquals("2029-05-15", response.licenseExpiryDate());
        assertEquals("+94779876543", response.phoneNumber());
        assertEquals("Kandy", response.serviceArea());
    }

    @Test
    @DisplayName("Should update availability to AVAILABLE when driver has at least one vehicle")
    void updateAvailabilityStatus_ToAvailable_WithVehicle_Success() {
        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(testVehicle));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateAvailabilityStatus("usr-100", AvailabilityStatus.AVAILABLE);

        assertEquals(AvailabilityStatus.AVAILABLE, response.availabilityStatus());
    }

    @Test
    @DisplayName("Should reject setting status to AVAILABLE if driver has no registered vehicles")
    void updateAvailabilityStatus_ToAvailable_NoVehicle_Throws() {
        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () ->
                driverService.updateAvailabilityStatus("usr-100", AvailabilityStatus.AVAILABLE));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should update simulated GPS location")
    void updateCurrentLocation_Success() {
        UpdateLocationRequest request = new UpdateLocationRequest(6.9350, 79.8500);

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateCurrentLocation("usr-100", request);

        assertNotNull(response.currentLocation());
        assertEquals(6.9350, response.currentLocation().getLatitude());
        assertEquals(79.8500, response.currentLocation().getLongitude());
    }

    @Test
    @DisplayName("Should retrieve eligible available drivers sorted by distance")
    void findEligibleDrivers_FiltersAndSorts() {
        testDriver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        Driver farDriver = new Driver(
                "drv-002",
                "usr-200",
                "B7654321",
                "2028-12-31",
                "+94772345678",
                AvailabilityStatus.AVAILABLE,
                "Colombo",
                new Location(6.9500, 79.8800)
        );

        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(farDriver, testDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(testVehicle));
        when(vehicleRepository.findByDriverId("drv-002")).thenReturn(List.of(testVehicle));

        List<EligibleDriverResponse> eligible = driverService.findEligibleDrivers("Colombo", 6.9271, 79.8612, 10.0);

        assertEquals(2, eligible.size());
        assertEquals("drv-001", eligible.get(0).driverId()); // closer driver comes first
        assertTrue(eligible.get(0).distanceKm() < eligible.get(1).distanceKm());
    }
}
