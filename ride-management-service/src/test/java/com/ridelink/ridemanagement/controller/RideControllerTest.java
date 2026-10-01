/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.ridelink.ridemanagement.RideManagementApplication
 *  com.ridelink.ridemanagement.client.AccountServiceClient
 *  com.ridelink.ridemanagement.client.DriverServiceClient
 *  com.ridelink.ridemanagement.client.dto.AccountUserDto
 *  com.ridelink.ridemanagement.client.dto.DriverProfileDto
 *  com.ridelink.ridemanagement.client.dto.VehicleDto
 *  com.ridelink.ridemanagement.dto.LocationDto
 *  com.ridelink.ridemanagement.dto.request.AssignDriverRequest
 *  com.ridelink.ridemanagement.dto.request.CancelRideRequest
 *  com.ridelink.ridemanagement.dto.request.CreateRideRequest
 *  com.ridelink.ridemanagement.dto.request.UpdateRideRequest
 *  com.ridelink.ridemanagement.model.Location
 *  com.ridelink.ridemanagement.model.Ride
 *  com.ridelink.ridemanagement.model.RideStatus
 *  com.ridelink.ridemanagement.repository.RideRepository
 *  com.ridelink.ridemanagement.security.JwtService
 *  org.junit.jupiter.api.BeforeEach
 *  org.junit.jupiter.api.DisplayName
 *  org.junit.jupiter.api.Test
 *  org.mockito.ArgumentMatchers
 *  org.mockito.Mockito
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
 *  org.springframework.boot.test.context.SpringBootTest
 *  org.springframework.data.domain.PageImpl
 *  org.springframework.data.domain.Pageable
 *  org.springframework.http.MediaType
 *  org.springframework.test.context.ActiveProfiles
 *  org.springframework.test.context.bean.override.mockito.MockitoBean
 *  org.springframework.test.web.servlet.MockMvc
 *  org.springframework.test.web.servlet.RequestBuilder
 *  org.springframework.test.web.servlet.request.MockMvcRequestBuilders
 *  org.springframework.test.web.servlet.result.MockMvcResultMatchers
 */
package com.ridelink.ridemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ridemanagement.RideManagementApplication;
import com.ridelink.ridemanagement.client.AccountServiceClient;
import com.ridelink.ridemanagement.client.DriverServiceClient;
import com.ridelink.ridemanagement.client.dto.AccountUserDto;
import com.ridelink.ridemanagement.client.dto.DriverProfileDto;
import com.ridelink.ridemanagement.client.dto.VehicleDto;
import com.ridelink.ridemanagement.dto.LocationDto;
import com.ridelink.ridemanagement.dto.request.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.UpdateRideRequest;
import com.ridelink.ridemanagement.model.Location;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.security.JwtService;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes={RideManagementApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles(value={"test"})
@DisplayName(value="RideController Integration and RBAC Security Tests")
class RideControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @MockitoBean
    private RideRepository rideRepository;
    @MockitoBean
    private AccountServiceClient accountServiceClient;
    @MockitoBean
    private DriverServiceClient driverServiceClient;
    private String passengerToken;
    private String driverToken;
    private String adminToken;
    private Ride sampleRide;
    private AccountUserDto sampleAccount;

    RideControllerTest() {
    }

    @BeforeEach
    void setUp() {
        this.passengerToken = "Bearer " + this.jwtService.generateToken("passenger123", "passenger@ridelink.com", "PASSENGER");
        this.driverToken = "Bearer " + this.jwtService.generateToken("driver123", "driver@ridelink.com", "DRIVER");
        this.adminToken = "Bearer " + this.jwtService.generateToken("admin123", "admin@ridelink.com", "ADMIN");
        this.sampleRide = new Ride("passenger123", new Location("SLIIT Malabe", Double.valueOf(6.9147), Double.valueOf(79.9729)), new Location("Fort", Double.valueOf(6.9344), Double.valueOf(79.8428)));
        this.sampleRide.setId("ride123");
        this.sampleRide.setDriverId("driver123");
        this.sampleRide.setStatus(RideStatus.REQUESTED);
        this.sampleAccount = new AccountUserDto("passenger123", "Kasun Perera", "passenger@ridelink.com", "+94771234567", "PASSENGER", "ACTIVE", Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/v1/rides creates ride and returns 201 Created")
    void testCreateRide_Success() throws Exception {
        CreateRideRequest request = new CreateRideRequest(null, new LocationDto("SLIIT Malabe", 6.9147, 79.9729), new LocationDto("Fort", 6.9344, 79.8428));
        Mockito.when(this.accountServiceClient.getUserById(ArgumentMatchers.eq("passenger123"), ArgumentMatchers.any())).thenReturn(this.sampleAccount);
        Mockito.when(this.rideRepository.existsByPassengerIdAndStatusIn(ArgumentMatchers.eq("passenger123"), ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId("rideGenerated123");
            return r;
        });
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rides").header("Authorization", this.passengerToken).contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true)).andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value("rideGenerated123")).andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    @DisplayName("POST /api/v1/rides without token returns 401 Unauthorized")
    void testCreateRide_NoToken_Returns401() throws Exception {
        CreateRideRequest request = new CreateRideRequest(null, new LocationDto("SLIIT Malabe", 6.9147, 79.9729), new LocationDto("Fort", 6.9344, 79.8428));
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rides").contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/rides with invalid coordinates returns 400 Bad Request")
    void testCreateRide_ValidationFailure() throws Exception {
        CreateRideRequest request = new CreateRideRequest(null, new LocationDto("", 190.0, 79.9729), new LocationDto("Fort", 6.9344, 79.8428));
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rides").header("Authorization", this.passengerToken).contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400)).andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("GET /api/v1/rides/{id} returns 200 OK and RideResponse")
    void testGetRideById_Success() throws Exception {
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rides/ride123").header("Authorization", this.passengerToken)).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true)).andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value("ride123"));
    }

    @Test
    @DisplayName("PUT /api/v1/rides/{id} updates ride and returns 200 OK")
    void testUpdateRide_Success() throws Exception {
        UpdateRideRequest request = new UpdateRideRequest(new LocationDto("New Pickup", 6.92, 79.98), new LocationDto("New Destination", 6.94, 79.85));
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        this.mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/rides/ride123").header("Authorization", this.passengerToken).contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/rides/{id} returns 204 No Content for ADMIN")
    void testDeleteRide_AdminSuccess() throws Exception {
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rides/ride123").header("Authorization", this.adminToken)).andExpect(MockMvcResultMatchers.status().isNoContent());
        Mockito.verify(this.rideRepository).delete(this.sampleRide);
    }

    @Test
    @DisplayName("DELETE /api/v1/rides/{id} returns 403 Forbidden for PASSENGER")
    void testDeleteRide_PassengerForbidden() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/rides/ride123").header("Authorization", this.passengerToken)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/rides/{id}/assign-driver returns 200 OK for ADMIN")
    void testAssignDriver_Success() throws Exception {
        AssignDriverRequest request = new AssignDriverRequest("driver123", "vehicle123");
        DriverProfileDto profile = new DriverProfileDto("driver123", "u1", "L1", "2028-12-31", "+94771", "AVAILABLE", "Colombo", null, Instant.now(), Instant.now());
        VehicleDto vehicle = new VehicleDto("vehicle123", "driver123", "Toyota", "Prius", 2020, "WP-123", "CAR", "White");
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.driverServiceClient.getDriverById(ArgumentMatchers.eq("driver123"), ArgumentMatchers.any())).thenReturn(profile);
        Mockito.when(this.driverServiceClient.getVehicleById(ArgumentMatchers.eq("vehicle123"), ArgumentMatchers.any())).thenReturn(vehicle);
        Mockito.when(this.rideRepository.existsByDriverIdAndStatusIn(ArgumentMatchers.eq("driver123"), ArgumentMatchers.any())).thenReturn(false);
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        this.mockMvc.perform((RequestBuilder)MockMvcRequestBuilders.patch((String)"/api/v1/rides/ride123/assign-driver", (Object[])new Object[0]).header("Authorization", new Object[]{this.adminToken}).contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString((Object)request))).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath((String)"$.success", (Object[])new Object[0]).value((Object)true));
    }

    @Test
    @DisplayName(value="PATCH /api/v1/rides/{id}/cancel returns 200 OK")
    void testCancelRide_Success() throws Exception {
        CancelRideRequest request = new CancelRideRequest("Change of plans");
        Mockito.when(this.rideRepository.findById("ride123")).thenReturn(Optional.of(this.sampleRide));
        Mockito.when(this.rideRepository.save(ArgumentMatchers.any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        this.mockMvc.perform((RequestBuilder)MockMvcRequestBuilders.patch((String)"/api/v1/rides/ride123/cancel", (Object[])new Object[0]).header("Authorization", new Object[]{this.passengerToken}).contentType(MediaType.APPLICATION_JSON).content(this.objectMapper.writeValueAsString((Object)request))).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath((String)"$.success", (Object[])new Object[0]).value((Object)true));
    }

    @Test
    @DisplayName(value="GET /api/v1/rides/passenger/{passengerId} returns 200 OK with history")
    void testGetPassengerRideHistory_Success() throws Exception {
        Mockito.when((Object)this.rideRepository.findByPassengerId((String)ArgumentMatchers.eq((Object)"passenger123"), (Pageable)ArgumentMatchers.any(Pageable.class))).thenReturn((Object)new PageImpl(List.of(this.sampleRide)));
        this.mockMvc.perform((RequestBuilder)MockMvcRequestBuilders.get((String)"/api/v1/rides/passenger/passenger123", (Object[])new Object[0]).header("Authorization", new Object[]{this.passengerToken})).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath((String)"$.success", (Object[])new Object[0]).value((Object)true)).andExpect(MockMvcResultMatchers.jsonPath((String)"$.data.content[0].id", (Object[])new Object[0]).value((Object)"ride123"));
    }
}
