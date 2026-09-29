package com.ridelink.drivervehicleservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard uniform JSON response wrapper for successful API responses.
 *
 * @param <T> payload type
 */
@Schema(description = "Standard API response wrapper")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @Schema(description = "Indicates whether the operation succeeded", example = "true")
        boolean success,

        @Schema(description = "Descriptive operation outcome message", example = "Operation completed successfully")
        String message,

        @Schema(description = "Response payload data")
        T data,

        @Schema(description = "Timestamp when the response was generated (ISO-8601)", example = "2026-09-28T00:00:00Z")
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, Instant.now());
    }
}
