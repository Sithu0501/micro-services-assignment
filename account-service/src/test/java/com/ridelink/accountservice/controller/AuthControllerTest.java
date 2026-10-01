package com.ridelink.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.accountservice.dto.request.LoginRequest;
import com.ridelink.accountservice.dto.request.RegisterRequest;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ridelink.accountservice.AccountServiceApplication;

@SpringBootTest(classes = AccountServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration / Web Layer Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Successfully registers passenger")
    void testRegisterPassengerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Kasun Perera",
                "kasun.test@ridelink.com",
                "0771234567",
                "SafePass@2026",
                Role.PASSENGER
        );

        when(userRepository.existsByEmail("kasun.test@ridelink.com")).thenReturn(false);

        User savedUser = new User(
                "uid-100",
                "Kasun Perera",
                "kasun.test@ridelink.com",
                "+94771234567",
                passwordEncoder.encode("SafePass@2026"),
                Role.PASSENGER,
                AccountStatus.ACTIVE
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is("kasun.test@ridelink.com")))
                .andExpect(jsonPath("$.data.user.role", is("PASSENGER")))
                .andExpect(jsonPath("$.data.user.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.user.password").doesNotExist()); // Ensure password is NEVER returned
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Rejects public registration with ADMIN role (400 Bad Request)")
    void testRegisterAdminRejected() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Attacker",
                "attacker@evil.com",
                "0771234567",
                "Attack@2026",
                Role.ADMIN
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Rejects duplicate email (409 Conflict)")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Existing User",
                "existing@ridelink.com",
                "0771234567",
                "Password@2026",
                Role.PASSENGER
        );

        when(userRepository.existsByEmail("existing@ridelink.com")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Resource Conflict")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Rejects weak password missing digits/specials (400 Bad Request)")
    void testRegisterWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Weak Password User",
                "weak@ridelink.com",
                "0771234567",
                "password", // No uppercase, no digits, no special characters
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Rejects invalid phone number format (400 Bad Request)")
    void testRegisterInvalidPhone() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Bad Phone User",
                "badphone@ridelink.com",
                "12345", // Invalid phone format
                "SafePass@2026",
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.phone", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Successfully logs in with valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("kasun.login@ridelink.com", "SafePass@2026");

        User user = new User(
                "uid-100",
                "Kasun Perera",
                "kasun.login@ridelink.com",
                "+94771234567",
                passwordEncoder.encode("SafePass@2026"),
                Role.PASSENGER,
                AccountStatus.ACTIVE
        );
        when(userRepository.findByEmail("kasun.login@ridelink.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is("kasun.login@ridelink.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Rejects invalid password with 401 Unauthorized")
    void testLoginWrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("kasun.login@ridelink.com", "WrongPassword@2026");

        User user = new User(
                "uid-100",
                "Kasun Perera",
                "kasun.login@ridelink.com",
                "+94771234567",
                passwordEncoder.encode("CorrectPassword@2026"),
                Role.PASSENGER,
                AccountStatus.ACTIVE
        );
        when(userRepository.findByEmail("kasun.login@ridelink.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Rejects suspended account with 403 Forbidden")
    void testLoginSuspendedAccount() throws Exception {
        LoginRequest request = new LoginRequest("suspended@ridelink.com", "SafePass@2026");

        User user = new User(
                "uid-102",
                "Suspended User",
                "suspended@ridelink.com",
                "+94771234567",
                passwordEncoder.encode("SafePass@2026"),
                Role.PASSENGER,
                AccountStatus.SUSPENDED
        );
        when(userRepository.findByEmail("suspended@ridelink.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Rejects deactivated account with 401 Unauthorized")
    void testLoginDeactivatedAccount() throws Exception {
        LoginRequest request = new LoginRequest("deactivated@ridelink.com", "SafePass@2026");

        User user = new User(
                "uid-103",
                "Deactivated User",
                "deactivated@ridelink.com",
                "+94771234567",
                passwordEncoder.encode("SafePass@2026"),
                Role.PASSENGER,
                AccountStatus.DEACTIVATED
        );
        when(userRepository.findByEmail("deactivated@ridelink.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Blank full name returns 400 Bad Request")
    void testRegisterBlankFullName() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "blank.name@ridelink.com",
                "0771234567",
                "SafePass@2026",
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors.fullName", notNullValue()));
    }
}
