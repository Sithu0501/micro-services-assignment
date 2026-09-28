package com.ridelink.drivervehicleservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service managing JWT validation and claim extraction for the Driver & Vehicle Service.
 *
 * Implements stateless cryptographic token validation using the shared JWT_SECRET.
 * Does not require external database lookups to the Account Service.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long jwtExpiration;

    public JwtService(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpiration
    ) {
        this.secret = secret;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Build the cryptographic signing key for HMAC-SHA algorithms.
     * Ensures minimum 256 bits length (SHA-256 fallback if secret is too short).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                log.error("SHA-256 algorithm not available for JWT key hashing", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract user email from token claims (or subject if email is absent).
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String email = claims.get("email", String.class);
        return (email != null && !email.isBlank()) ? email : claims.getSubject();
    }

    /**
     * Extract unique user identifier (Account Service userId) from token.
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String userId = claims.get("userId", String.class);
        return (userId != null && !userId.isBlank()) ? userId : claims.getSubject();
    }

    /**
     * Extract user role from token claims (e.g., "DRIVER", "PASSENGER", "ADMIN").
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract expiration date from token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extraction utility.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    /**
     * Cryptographically validate token and verify it is not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token has expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Parse all claims from signed token.
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Failed to parse JWT claims: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper to generate signed tokens for integration/unit testing.
     */
    public String generateToken(String userId, String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("email", email);
        extraClaims.put("role", role != null ? role : "DRIVER");

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
