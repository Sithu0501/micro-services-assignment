package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.request.LoginRequest;
import com.ridelink.accountservice.dto.request.RegisterRequest;
import com.ridelink.accountservice.dto.response.LoginResponse;
import com.ridelink.accountservice.dto.response.RegisterResponse;
import com.ridelink.accountservice.exception.AccountDisabledException;
import com.ridelink.accountservice.exception.BadRequestException;
import com.ridelink.accountservice.exception.DuplicateResourceException;
import com.ridelink.accountservice.exception.InvalidCredentialsException;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Successful passenger registration assigns default role PASSENGER and ACTIVE status")
    void testRegisterPassengerSuccess() {
        RegisterRequest request = new RegisterRequest(
                "Kasun Perera",
                "kasun@example.com",
                "0771234567",
                "StrongPass@123",
                null // Omitted role should default to PASSENGER
        );

        when(userRepository.existsByEmail("kasun@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass@123")).thenReturn("encodedPassword123");

        User savedUser = new User("uid-100", "Kasun Perera", "kasun@example.com", "+94771234567", "encodedPassword123", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString(), anyString(), any(Role.class))).thenReturn("mock-jwt-token");
        when(jwtService.getJwtExpiration()).thenReturn(86400000L);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("uid-100", response.user().id());
        assertEquals(Role.PASSENGER, response.user().role());
        assertEquals(AccountStatus.ACTIVE, response.user().status());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Successful driver registration when DRIVER role requested")
    void testRegisterDriverSuccess() {
        RegisterRequest request = new RegisterRequest(
                "Sunil Shantha",
                "sunil.driver@example.com",
                "+94712345678",
                "Driver@2026",
                Role.DRIVER
        );

        when(userRepository.existsByEmail("sunil.driver@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");

        User savedUser = new User("uid-101", "Sunil Shantha", "sunil.driver@example.com", "+94712345678", "encodedPassword123", Role.DRIVER, AccountStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(anyString(), anyString(), any(Role.class))).thenReturn("mock-jwt-token");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(Role.DRIVER, response.user().role());
    }

    @Test
    @DisplayName("Public registration attempting ADMIN role is rejected with BadRequestException")
    void testRegisterAdminRejected() {
        RegisterRequest request = new RegisterRequest(
                "Hacker User",
                "hacker@example.com",
                "0771234567",
                "Hacker@1234",
                Role.ADMIN
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("ADMIN"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Duplicate email registration fails with DuplicateResourceException")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Kasun Perera",
                "kasun@example.com",
                "0771234567",
                "StrongPass@123",
                Role.PASSENGER
        );

        when(userRepository.existsByEmail("kasun@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Successful login with valid credentials")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("kasun@example.com", "StrongPass@123");
        User user = new User("uid-100", "Kasun Perera", "kasun@example.com", "+94771234567", "encodedPassword123", Role.PASSENGER, AccountStatus.ACTIVE);

        when(userRepository.findByEmail("kasun@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPass@123", "encodedPassword123")).thenReturn(true);
        when(jwtService.generateToken(user.getId(), user.getEmail(), user.getRole())).thenReturn("mock-login-token");
        when(jwtService.getJwtExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-login-token", response.accessToken());
        assertEquals("uid-100", response.user().id());
    }

    @Test
    @DisplayName("Login with unknown email fails with InvalidCredentialsException")
    void testLoginUnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", "Password@123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login with incorrect password fails with InvalidCredentialsException")
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest("kasun@example.com", "WrongPassword@123");
        User user = new User("uid-100", "Kasun Perera", "kasun@example.com", "+94771234567", "encodedPassword123", Role.PASSENGER, AccountStatus.ACTIVE);

        when(userRepository.findByEmail("kasun@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword@123", "encodedPassword123")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login with SUSPENDED account fails with AccountDisabledException")
    void testLoginSuspendedAccount() {
        LoginRequest request = new LoginRequest("suspended@example.com", "Password@123");
        User user = new User("uid-102", "Suspended User", "suspended@example.com", "+94771234567", "encodedPassword123", Role.PASSENGER, AccountStatus.SUSPENDED);

        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(user));

        AccountDisabledException ex = assertThrows(AccountDisabledException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("suspended"));
    }

    @Test
    @DisplayName("Login with DEACTIVATED account fails with AccountDisabledException")
    void testLoginDeactivatedAccount() {
        LoginRequest request = new LoginRequest("deactivated@example.com", "Password@123");
        User user = new User("uid-103", "Deactivated User", "deactivated@example.com", "+94771234567", "encodedPassword123", Role.PASSENGER, AccountStatus.DEACTIVATED);

        when(userRepository.findByEmail("deactivated@example.com")).thenReturn(Optional.of(user));

        AccountDisabledException ex = assertThrows(AccountDisabledException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("deactivated"));
    }
}
