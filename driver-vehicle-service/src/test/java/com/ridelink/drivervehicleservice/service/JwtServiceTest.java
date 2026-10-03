package com.ridelink.drivervehicleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private static final String TEST_SECRET = "test-only-jwt-secret-not-for-production-use-0123456789";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should correctly extract userId, email, and role from a valid token")
    void testExtractClaimsFromValidToken() {
        String token = jwtService.generateToken("user123", "driver@ridelink.com", "DRIVER");

        assertNotNull(token);
        assertEquals("user123", jwtService.extractUserId(token));
        assertEquals("driver@ridelink.com", jwtService.extractUsername(token));
        assertEquals("DRIVER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testInvalidTokenSignature() {
        String token = jwtService.generateToken("user123", "driver@ridelink.com", "DRIVER");
        JwtService otherService = new JwtService("differentSecretKeyMustBeLongEnough1234567890!", TEST_EXPIRATION);

        assertFalse(otherService.isTokenValid(token));
    }

    @Test
    @DisplayName("Should detect expired tokens")
    void testExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, -1000); // expired 1s ago
        String token = shortLivedJwtService.generateToken("user123", "driver@ridelink.com", "DRIVER");

        assertTrue(shortLivedJwtService.isTokenExpired(token));
        assertFalse(shortLivedJwtService.isTokenValid(token));
    }
}
