/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description="Standard API response wrapper")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(@Schema(description="Indicates whether the operation succeeded", example="true") boolean success, @Schema(description="Descriptive outcome message", example="Operation completed successfully") String message, @Schema(description="Response payload data") T data, @Schema(description="Timestamp when the response was generated (ISO-8601)", example="2026-10-01T10:00:00Z") Instant timestamp) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, "Operation successful", data, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<T>(true, message, null, Instant.now());
    }
}

