package com.ridelink.accountservice.service;

import com.ridelink.accountservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
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
 * Service managing JSON Web Tokens (JWT) lifecycle:
 * generation, cryptographic signing, parsing, claims extraction, and validation.
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
     * Generate token with user claims: subject (userId), email, and role.
     *
     * @param userId unique user ID
     * @param email  user email
     * @param role   user role
     * @return signed JWT string
     */
    public String generateToken(String userId, String email, Role role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("email", email);
        extraClaims.put("role", role != null ? role.name() : Role.PASSENGER.name());

        return buildToken(extraClaims, userId, jwtExpiration);
    }

    /**
     * Generate token with custom claims and expiration.
     */
    public String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username/email from token claims.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        // First check email claim; fallback to subject
        String email = claims.get("email", String.class);
        return (email != null && !email.isBlank()) ? email : claims.getSubject();
    }

    /**
     * Extract user identifier from token subject or userId claim.
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
     * Extract role from token claims.
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
     * Validate token against user details and expiration.
     *
     * @param token       JWT token string
     * @param userDetails Spring Security user details
     * @return true if valid and not expired, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equalsIgnoreCase(userDetails.getUsername())) && !isTokenExpired(token);
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

    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
