package com.ridelink.fare_payment_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class JwtService {
    private final String secret;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    private SecretKey signingKey() {
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        try {
            if (key.length < 32) key = MessageDigest.getInstance("SHA-256").digest(key);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        return Keys.hmacShaKeyFor(key);
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    public boolean isTokenValid(String token) {
        try { return claims(token).getExpiration().after(new Date()); }
        catch (JwtException | IllegalArgumentException | NullPointerException e) { return false; }
    }

    public String extractUserId(String token) {
        Claims claims = claims(token);
        String userId = claims.get("userId", String.class);
        return userId == null || userId.isBlank() ? claims.getSubject() : userId;
    }

    public String extractUsername(String token) {
        Claims claims = claims(token);
        String email = claims.get("email", String.class);
        return email == null || email.isBlank() ? claims.getSubject() : email;
    }

    public String extractRole(String token) {
        String role = claims(token).get("role", String.class);
        return role == null ? null : role.toUpperCase(java.util.Locale.ROOT);
    }
}
