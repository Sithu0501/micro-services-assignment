/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.Module
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.http.HttpStatus
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.security.web.access.AccessDeniedHandler
 *  org.springframework.stereotype.Component
 */
package com.ridelink.ridemanagement.security;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ridelink.ridemanagement.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler
implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAccessDeniedHandler() {
        this.objectMapper.registerModule((Module)new JavaTimeModule());
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", "Access is denied. You lack the necessary role or permissions to perform this action.", request.getRequestURI());
        this.objectMapper.writeValue((OutputStream)response.getOutputStream(), (Object)errorResponse);
    }
}

