/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description="Standardized error response payload")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @Schema(description="Timestamp when error occurred (ISO-8601)", example="2026-10-01T10:00:00Z")
    private Instant timestamp = Instant.now();
    @Schema(description="HTTP status code", example="400")
    private int status;
    @Schema(description="Short error category name", example="Validation Failed")
    private String error;
    @Schema(description="Human-readable explanation of error", example="Request validation failed")
    private String message;
    @Schema(description="Request URI path", example="/api/v1/rides")
    private String path;
    @Schema(description="Map of field-specific validation errors, if applicable")
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return this.fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}

