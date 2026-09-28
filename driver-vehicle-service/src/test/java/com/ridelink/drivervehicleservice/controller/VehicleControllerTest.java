package com.ridelink.drivervehicleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.drivervehicleservice.DriverVehicleServiceApplication;
import com.ridelink.drivervehicleservice.dto.request.CreateVehicleRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateVehicleRequest;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.model.VehicleType;
import com.ridelink.drivervehicleservice.repository.DriverRepository;
import com.ridelink.drivervehicleservice.repository.VehicleRepository;
import com.ridelink.drivervehicleservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DriverVehicleServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VehicleController Integration & Security Tests")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private DriverRepository driverRepository;

    @MockitoBean
    private VehicleRepository vehicleRepository;

    private String driverToken;
    private String passengerToken;
    private Driver sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        driverToken = "Bearer " + jwtService.generateToken("driver-user-1", "driver@ridelink.com", "DRIVER");
        passengerToken = "Bearer " + jwtService.generateToken("passenger-user-1", "passenger@ridelink.com", "PASSENGER");

        sampleDriver = new Driver();
        sampleDriver.setId("drv-001");
        sampleDriver.setUserId("driver-user-1");
        sampleDriver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        sampleVehicle = new Vehicle(
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
    @DisplayName("POST /api/v1/vehicles - 201 Created registers new vehicle")
    void testRegisterVehicleSuccess() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.existsByRegistrationNumber("CAB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.registrationNumber", is("CAB-1234")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - 400 Bad Request on invalid capacity")
    void testRegisterVehicleValidationFailure() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 0 // capacity < 1
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.capacity", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - 401 Unauthorized when missing token")
    void testRegisterVehicleUnauthenticated() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles - 403 Forbidden for PASSENGER role")
    void testRegisterVehicleForbiddenForPassenger() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest(
                "CAB-1234", "Toyota", "Prius", 2022, "White", VehicleType.SEDAN, 4
        );

        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/me - 200 OK returns driver vehicles")
    void testGetMyVehiclesSuccess() throws Exception {
        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(sampleVehicle));

        mockMvc.perform(get("/api/v1/vehicles/me")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].registrationNumber", is("CAB-1234")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - 200 OK gets vehicle by ID")
    void testGetVehicleByIdSuccess() throws Exception {
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(sampleVehicle));

        mockMvc.perform(get("/api/v1/vehicles/veh-001")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is("veh-001")));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - 404 Not Found when vehicle does not exist")
    void testGetVehicleByIdNotFound() throws Exception {
        when(vehicleRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/vehicles/unknown")
                        .header("Authorization", driverToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - 200 OK updates vehicle")
    void testUpdateVehicleSuccess() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Toyota", "Aqua", 2023, "Silver", VehicleType.SEDAN, 4);

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        mockMvc.perform(put("/api/v1/vehicles/veh-001")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - 403 Forbidden when modifying another driver's vehicle")
    void testUpdateVehicleForbiddenForNonOwner() throws Exception {
        UpdateVehicleRequest request = new UpdateVehicleRequest("Toyota", "Aqua", 2023, "Silver", VehicleType.SEDAN, 4);
        sampleVehicle.setDriverId("other-driver");

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(sampleVehicle));

        mockMvc.perform(put("/api/v1/vehicles/veh-001")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));
    }

    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - 200 OK deletes vehicle")
    void testDeleteVehicleSuccess() throws Exception {
        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("veh-001")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/vehicles/veh-001")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(vehicleRepository, times(1)).deleteById("veh-001");
    }
}
