package com.ridelink.drivervehicleservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.drivervehicleservice.DriverVehicleServiceApplication;
import com.ridelink.drivervehicleservice.dto.request.CreateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateAvailabilityRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateDriverRequest;
import com.ridelink.drivervehicleservice.dto.request.UpdateLocationRequest;
import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Location;
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
@DisplayName("DriverController Integration & Security Tests")
class DriverControllerTest {

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

        sampleDriver = new Driver(
                "drv-001",
                "driver-user-1",
                "B1234567",
                "2028-12-31",
                "+94771234567",
                AvailabilityStatus.UNAVAILABLE,
                "Colombo",
                new Location(6.9271, 79.8612)
        );

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
    @DisplayName("POST /api/v1/drivers - 201 Created for valid profile creation")
    void testCreateProfileSuccess() throws Exception {
        CreateDriverRequest request = new CreateDriverRequest("B1234567", "2028-12-31", "+94771234567", "Colombo");

        when(driverRepository.existsByUserId("driver-user-1")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.licenseNumber", is("B1234567")))
                .andExpect(jsonPath("$.data.serviceArea", is("Colombo")));
    }

    @Test
    @DisplayName("POST /api/v1/drivers - 400 Bad Request on blank license")
    void testCreateProfileValidationFailure() throws Exception {
        CreateDriverRequest request = new CreateDriverRequest("", "2028-12-31", "+94771234567", "Colombo");

        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.licenseNumber", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/drivers - 403 Forbidden for PASSENGER role")
    void testCreateProfileForbiddenForPassenger() throws Exception {
        CreateDriverRequest request = new CreateDriverRequest("B1234567", "2028-12-31", "+94771234567", "Colombo");

        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/drivers/me - 200 OK returns driver profile")
    void testGetMyProfileSuccess() throws Exception {
        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is("drv-001")))
                .andExpect(jsonPath("$.data.licenseNumber", is("B1234567")));
    }

    @Test
    @DisplayName("GET /api/v1/drivers/me - 404 Not Found if profile does not exist")
    void testGetMyProfileNotFound() throws Exception {
        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", driverToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("PUT /api/v1/drivers/me - 200 OK updates profile")
    void testUpdateMyProfileSuccess() throws Exception {
        UpdateDriverRequest request = new UpdateDriverRequest("2029-12-31", "+94779876543", "Kandy");

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        mockMvc.perform(put("/api/v1/drivers/me")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/v1/drivers/me/availability - 200 OK updates availability")
    void testUpdateAvailabilitySuccess() throws Exception {
        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest(AvailabilityStatus.AVAILABLE);

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(sampleVehicle));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        mockMvc.perform(patch("/api/v1/drivers/me/availability")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("PUT /api/v1/drivers/me/location - 200 OK updates location")
    void testUpdateLocationSuccess() throws Exception {
        UpdateLocationRequest request = new UpdateLocationRequest(6.9350, 79.8500);

        when(driverRepository.findByUserId("driver-user-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        mockMvc.perform(put("/api/v1/drivers/me/location")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("PUT /api/v1/drivers/me/location - 400 Bad Request on invalid coordinates")
    void testUpdateLocationInvalidCoords() throws Exception {
        UpdateLocationRequest request = new UpdateLocationRequest(105.0, 79.8500); // Lat > 90

        mockMvc.perform(put("/api/v1/drivers/me/location")
                        .header("Authorization", driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.latitude", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/drivers/eligible - 200 OK accessible by authenticated clients")
    void testGetEligibleDriversSuccess() throws Exception {
        sampleDriver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(sampleDriver));
        when(vehicleRepository.findByDriverId("drv-001")).thenReturn(List.of(sampleVehicle));

        mockMvc.perform(get("/api/v1/drivers/eligible")
                        .param("serviceArea", "Colombo")
                        .header("Authorization", passengerToken)) // Passengers/Ride Service can query!
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].driverId", is("drv-001")))
                .andExpect(jsonPath("$.data[0].vehicle.registrationNumber", is("CAB-1234")));
    }

    @Test
    @DisplayName("GET /api/v1/drivers/{id} - 200 OK gets driver by ID")
    void testGetDriverByIdSuccess() throws Exception {
        when(driverRepository.findById("drv-001")).thenReturn(Optional.of(sampleDriver));

        mockMvc.perform(get("/api/v1/drivers/drv-001")
                        .header("Authorization", passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is("drv-001")));
    }
}
