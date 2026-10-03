/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.jsonwebtoken.Claims
 *  io.jsonwebtoken.JwtException
 *  io.jsonwebtoken.Jwts
 *  io.jsonwebtoken.security.Keys
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.stereotype.Service
 */
package com.ridelink.ridemanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final String secretKeyString;

    public JwtService(@Value(value="${jwt.secret}") String secret) {
        this.secretKeyString = secret;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secretKeyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor((byte[])keyBytes);
    }

    public String extractUserId(String token) {
        Claims claims = this.extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String userId = (String)claims.get("userId", String.class);
        return userId != null && !userId.isBlank() ? userId : claims.getSubject();
    }

    public String extractUsername(String token) {
        Claims claims = this.extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String email = (String)claims.get("email", String.class);
        return email != null && !email.isBlank() ? email : claims.getSubject();
    }

    public String extractRole(String token) {
        Claims claims = this.extractAllClaims(token);
        if (claims == null) {
            return "PASSENGER";
        }
        String role = (String)claims.get("role", String.class);
        return role != null ? role.toUpperCase() : "PASSENGER";
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = this.extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration == null || expiration.after(new Date());
        }
        catch (Exception e) {
            log.debug("JWT token validation failed: {}", (Object)e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return (Claims)Jwts.parser().verifyWith(this.getSigningKey()).build().parseSignedClaims((CharSequence)token).getPayload();
        }
        catch (JwtException | IllegalArgumentException e) {
            log.debug("Failed to extract claims from JWT: {}", (Object)e.getMessage());
            return null;
        }
    }

    public String generateToken(String userId, String email, String role) {
        return Jwts.builder().subject(userId).claim("userId", (Object)userId).claim("email", (Object)email).claim("role", (Object)(role != null ? role.toUpperCase() : "PASSENGER")).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 86400000L)).signWith((Key)this.getSigningKey()).compact();
    }
}

