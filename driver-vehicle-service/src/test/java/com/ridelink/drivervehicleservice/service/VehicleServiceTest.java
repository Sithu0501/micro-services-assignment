package com.ridelink.drivervehicleservice.service;

import com.ridelink.drivervehicleservice.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.response.VehicleResponse;
import com.ridelink.drivervehicleservice.exception.DuplicateResourceException;
import com.ridelink.drivervehicleservice.exception.ResourceNotFoundException;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.model.VehicleType;
import com.ridelink.drivervehicleservice.repository.DriverRepository;
import com.ridelink.drivervehicleservice.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Driver testDriver;
    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testDriver = new Driver();
        testDriver.setId("drv-001");
        testDriver.setUserId("usr-100");
        testDriver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

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
    @DisplayName("Should successfully register a new vehicle")
    void registerVehicle_Success() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.existsByRegistrationNumber("CAB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId("veh-001");
            return v;
        });

        VehicleResponse response = vehicleService.registerVehicle("usr-100", request);

        assertNotNull(response);
        assertEquals("CAB-1234", response.registrationNumber());
        assertEquals("drv-001", response.driverId());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when registration number already exists")
    void registerVehicle_DuplicatePlate_Throws() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.existsByRegistrationNumber("CAB-1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.registerVehicle("usr-100", request));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when registering vehicle without driver profile")
    void registerVehicle_NoDriverProfile_Throws() {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.registerVehicle("usr-100", request));
    }

    @Test
    @DisplayName("Should retrieve vehicles belonging to the driver")
    void getVehiclesByUserId_Success() {
        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(testVehicle));

        List<VehicleResponse> vehicles = vehicleService.getVehiclesByUserId("usr-100");

        assertEquals(1, vehicles.size());
        assertEquals("CAB-1234", vehicles.get(0).registrationNumber());
    }

    @Test
    @DisplayName("Should retrieve vehicle by ID")
    void getVehicleById_Success() {
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(testVehicle));

        VehicleResponse response = vehicleService.getVehicleById("veh-001");

        assertNotNull(response);
        assertEquals("veh-001", response.id());
    }

    @Test
    @DisplayName("Should update vehicle when owned by driver")
    void updateVehicle_Success() {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Toyota", "Aqua", 2023, "Silver", VehicleType.SEDAN, 4);

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse response = vehicleService.updateVehicle("usr-100", "veh-001", request);

        assertEquals("Aqua", response.model());
        assertEquals("Silver", response.color());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when updating vehicle owned by someone else")
    void updateVehicle_NotOwner_Throws() {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Toyota", "Aqua", 2023, "Silver", VehicleType.SEDAN, 4);
        testVehicle.setDriverId("other-driver");

        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(testVehicle));

        assertThrows(AccessDeniedException.class, () -> vehicleService.updateVehicle("usr-100", "veh-001", request));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should delete vehicle and revert driver to UNAVAILABLE if no vehicles remain")
    void deleteVehicle_LastVehicle_ResetsAvailability() {
        when(driverRepository.findByUserId("usr-100")).thenReturn(Optional.of(testDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(Collections.emptyList());

        vehicleService.deleteVehicle("usr-100", "veh-001");

        verify(vehicleRepository, times(1)).deleteById("veh-001");
        assertEquals(AvailabilityStatus.UNAVAILABLE, testDriver.getAvailabilityStatus());
        verify(driverRepository, times(1)).save(testDriver);
    }
}
