package com.ridelink.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.accountservice.dto.request.CreateUserRequest;
import com.ridelink.accountservice.dto.request.UpdateAccountStatusRequest;
import com.ridelink.accountservice.dto.request.UpdateProfileRequest;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import com.ridelink.accountservice.service.JwtService;
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

import com.ridelink.accountservice.AccountServiceApplication;

@SpringBootTest(classes = AccountServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration and RBAC Security Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private String passengerToken;
    private String adminToken;
    private User passengerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        passengerUser = new User("passenger-id-1", "Passenger Kasun", "passenger@ridelink.com", "+94771234567", "passHash", Role.PASSENGER, AccountStatus.ACTIVE);
        adminUser = new User("admin-id-1", "Admin Sithum", "admin@ridelink.com", "+94777654321", "adminHash", Role.ADMIN, AccountStatus.ACTIVE);

        when(userRepository.findByEmail("passenger@ridelink.com")).thenReturn(Optional.of(passengerUser));
        when(userRepository.findByEmail("admin@ridelink.com")).thenReturn(Optional.of(adminUser));

        passengerToken = "Bearer " + jwtService.generateToken(passengerUser.getId(), passengerUser.getEmail(), passengerUser.getRole());
        adminToken = "Bearer " + jwtService.generateToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Returns 200 with profile for authenticated user")
    void testGetOwnProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("passenger@ridelink.com")))
                .andExpect(jsonPath("$.data.fullName", is("Passenger Kasun")))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Returns 401 when token is missing")
    void testGetProfileUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me - Updates own profile successfully")
    void testUpdateOwnProfileSuccess() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Updated Kasun", null, "0778888888");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Updated Kasun")))
                .andExpect(jsonPath("$.data.phone", is("+94778888888")));
    }

    @Test
    @DisplayName("GET /api/v1/users - RBAC: Passenger is forbidden from listing users (403 Forbidden)")
    void testGetAllUsersAsPassengerForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /api/v1/users - RBAC: Admin is permitted to list users (200 OK)")
    void testGetAllUsersAsAdminSuccess() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(passengerUser, adminUser));

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/v1/users - RBAC: Passenger is forbidden from creating users directly (403 Forbidden)")
    void testCreateUserAsPassengerForbidden() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "New Driver",
                "driver@ridelink.com",
                "0771234567",
                "Driver@2026",
                Role.DRIVER,
                AccountStatus.ACTIVE
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("POST /api/v1/users - Admin successfully creates a new account (201 Created)")
    void testCreateUserAsAdminSuccess() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "New Driver",
                "driver@ridelink.com",
                "0771234567",
                "Driver@2026",
                Role.DRIVER,
                AccountStatus.ACTIVE
        );

        when(userRepository.existsByEmail("driver@ridelink.com")).thenReturn(false);
        User createdUser = new User("driver-id-1", "New Driver", "driver@ridelink.com", "+94771234567", "hash", Role.DRIVER, AccountStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("driver@ridelink.com")))
                .andExpect(jsonPath("$.data.role", is("DRIVER")));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/status - RBAC: Passenger is forbidden (403 Forbidden)")
    void testUpdateStatusAsPassengerForbidden() throws Exception {
        UpdateAccountStatusRequest request = new UpdateAccountStatusRequest(AccountStatus.SUSPENDED);

        mockMvc.perform(patch("/api/v1/users/passenger-id-1/status")
                        .header("Authorization", passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/status - Admin updates account status to SUSPENDED (200 OK)")
    void testUpdateStatusAsAdminSuccess() throws Exception {
        UpdateAccountStatusRequest request = new UpdateAccountStatusRequest(AccountStatus.SUSPENDED);

        when(userRepository.findById("passenger-id-1")).thenReturn(Optional.of(passengerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/v1/users/passenger-id-1/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("SUSPENDED")));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - RBAC: Passenger is forbidden from deleting accounts (403 Forbidden)")
    void testDeleteUserAsPassengerForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/passenger-id-1")
                        .header("Authorization", passengerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Admin successfully deletes user (204 No Content)")
    void testDeleteUserAsAdminSuccess() throws Exception {
        when(userRepository.findById("passenger-id-1")).thenReturn(Optional.of(passengerUser));

        mockMvc.perform(delete("/api/v1/users/passenger-id-1")
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        verify(userRepository).delete(passengerUser);
    }
}
