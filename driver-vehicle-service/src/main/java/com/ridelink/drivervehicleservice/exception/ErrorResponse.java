package com.ridelink.drivervehicleservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error payload returned across all endpoints on failures.
 * Never leaks stack traces or internal implementation details.
 */
@Schema(description = "Standardized error response payload")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Timestamp when error occurred (ISO-8601)", example = "2026-09-28T00:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Short error category name", example = "Validation Failed")
    private String error;

    @Schema(description = "Human-readable explanation of error", example = "Request validation failed")
    private String message;

    @Schema(description = "Request URI path", example = "/api/v1/drivers/me")
    private String path;

    @Schema(description = "Map of field-specific validation errors, if applicable")
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
