/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.core.ParameterizedTypeReference
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.stereotype.Component
 *  org.springframework.web.client.ResourceAccessException
 *  org.springframework.web.client.RestClient
 */
package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.DriverProfileDto;
import com.ridelink.ridemanagement.client.dto.EligibleDriverDto;
import com.ridelink.ridemanagement.client.dto.VehicleDto;
import com.ridelink.ridemanagement.dto.response.ApiResponse;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import com.ridelink.ridemanagement.exception.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class DriverServiceClient {
    private static final Logger log = LoggerFactory.getLogger(DriverServiceClient.class);
    private final RestClient driverServiceRestClient;

    public DriverServiceClient(RestClient driverServiceRestClient) {
        this.driverServiceRestClient = driverServiceRestClient;
    }

    public DriverProfileDto getDriverById(String driverId, String bearerToken) {
        try {
            ApiResponse<DriverProfileDto> response = this.driverServiceRestClient.get()
                .uri("/api/v1/drivers/{id}", driverId)
                .header("Authorization", this.normalizeBearerToken(bearerToken))
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new ResourceNotFoundException("Driver not found with ID: " + driverId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ExternalServiceException("Driver Service returned server error: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<ApiResponse<DriverProfileDto>>() {});
            if (response != null && response.data() != null) {
                return response.data();
            }
            throw new ResourceNotFoundException("Driver not found with ID: " + driverId);
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (ResourceAccessException e) {
            log.error("Failed to connect to Driver Service: {}", (Object)e.getMessage());
            throw new ExternalServiceException("Driver Service is currently unavailable. Please try again later.", e);
        }
        catch (Exception e) {
            if (e instanceof ExternalServiceException) {
                throw (ExternalServiceException)e;
            }
            log.error("Error communicating with Driver Service for driver {}: {}", (Object)driverId, (Object)e.getMessage());
            throw new ExternalServiceException("Error communicating with Driver Service: " + e.getMessage(), e);
        }
    }

    public List<EligibleDriverDto> getEligibleDrivers(Double latitude, Double longitude, Double radiusKm, String serviceArea, String bearerToken) {
        try {
            ApiResponse<List<EligibleDriverDto>> response = this.driverServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/drivers/eligible")
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("radius", radiusKm != null ? radiusKm : 10.0)
                    .queryParamIfPresent("serviceArea", Optional.ofNullable(serviceArea))
                    .build())
                .header("Authorization", this.normalizeBearerToken(bearerToken))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ExternalServiceException("Driver Service returned server error during discovery: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<ApiResponse<List<EligibleDriverDto>>>() {});
            if (response != null && response.data() != null) {
                return response.data();
            }
            return Collections.emptyList();
        }
        catch (ResourceAccessException e) {
            log.error("Failed to connect to Driver Service discovery: {}", (Object)e.getMessage());
            throw new ExternalServiceException("Driver Service is currently unavailable for dispatch. Please try again later.", e);
        }
        catch (Exception e) {
            if (e instanceof ExternalServiceException) {
                throw (ExternalServiceException)e;
            }
            log.error("Error discovering eligible drivers: {}", (Object)e.getMessage());
            throw new ExternalServiceException("Error communicating with Driver Service: " + e.getMessage(), e);
        }
    }

    public VehicleDto getVehicleById(String vehicleId, String bearerToken) {
        try {
            ApiResponse<VehicleDto> response = this.driverServiceRestClient.get()
                .uri("/api/v1/vehicles/{id}", vehicleId)
                .header("Authorization", this.normalizeBearerToken(bearerToken))
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ExternalServiceException("Driver Service returned server error for vehicle: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<ApiResponse<VehicleDto>>() {});
            if (response != null && response.data() != null) {
                return response.data();
            }
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (ResourceAccessException e) {
            log.error("Failed to connect to Driver Service for vehicle {}: {}", (Object)vehicleId, (Object)e.getMessage());
            throw new ExternalServiceException("Driver Service is currently unavailable. Please try again later.", e);
        }
        catch (Exception e) {
            if (e instanceof ExternalServiceException) {
                throw (ExternalServiceException)e;
            }
            log.error("Error retrieving vehicle {}: {}", (Object)vehicleId, (Object)e.getMessage());
            throw new ExternalServiceException("Error communicating with Driver Service: " + e.getMessage(), e);
        }
    }

    private String normalizeBearerToken(String token) {
        if (token == null) {
            return "";
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}

