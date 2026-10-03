package com.ridelink.accountservice.service;

import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "test-only-jwt-secret-not-for-production-use-0123456789";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expiration);
    }

    @Test
    @DisplayName("Generate token successfully and extract claims")
    void testGenerateTokenAndExtractClaims() {
        String userId = "64f1a2b3c4d5e6f7a8b9c0d1";
        String email = "test.user@ridelink.com";
        Role role = Role.PASSENGER;

        String token = jwtService.generateToken(userId, email, role);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(Role.PASSENGER.name(), jwtService.extractRole(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Validate token against matching UserDetails")
    void testTokenValidationSuccess() {
        User user = new User("64f1a2b3c4d5e6f7a8b9c0d1", "Test User", "test.user@ridelink.com", "+94771234567", "pass", Role.PASSENGER, AccountStatus.ACTIVE);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("Token validation fails when email does not match UserDetails")
    void testTokenValidationMismatch() {
        User user1 = new User("id1", "User One", "user1@ridelink.com", "+94771234567", "pass", Role.PASSENGER, AccountStatus.ACTIVE);
        User user2 = new User("id2", "User Two", "user2@ridelink.com", "+94771234567", "pass", Role.PASSENGER, AccountStatus.ACTIVE);

        String token = jwtService.generateToken(user1.getId(), user1.getEmail(), user1.getRole());

        assertFalse(jwtService.isTokenValid(token, new CustomUserDetails(user2)));
    }

    @Test
    @DisplayName("Expired token is recognized as expired")
    void testExpiredToken() {
        // Create token with negative expiration (-10 seconds)
        String token = jwtService.buildToken(Map.of("email", "test@test.com"), "user123", -10000L);

        assertTrue(jwtService.isTokenExpired(token));
    }
}
