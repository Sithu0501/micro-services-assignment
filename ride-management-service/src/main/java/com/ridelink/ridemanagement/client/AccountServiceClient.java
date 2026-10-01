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

import com.ridelink.ridemanagement.client.dto.AccountUserDto;
import com.ridelink.ridemanagement.dto.response.ApiResponse;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import com.ridelink.ridemanagement.exception.ResourceNotFoundException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class AccountServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);
    private final RestClient accountServiceRestClient;

    public AccountServiceClient(RestClient accountServiceRestClient) {
        this.accountServiceRestClient = accountServiceRestClient;
    }

    public AccountUserDto getUserById(String userId, String bearerToken) {
        try {
            ApiResponse<AccountUserDto> response = this.accountServiceRestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .header("Authorization", this.normalizeBearerToken(bearerToken))
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new ResourceNotFoundException("Passenger account not found with ID: " + userId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ExternalServiceException("Account Service returned server error: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<ApiResponse<AccountUserDto>>() {});
            if (response != null && response.data() != null) {
                return response.data();
            }
            throw new ResourceNotFoundException("Passenger account not found with ID: " + userId);
        }
        catch (ResourceNotFoundException e) {
            throw e;
        }
        catch (ResourceAccessException e) {
            log.error("Failed to connect to Account Service: {}", (Object)e.getMessage());
            throw new ExternalServiceException("Account Service is currently unavailable. Please try again later.", e);
        }
        catch (Exception e) {
            if (e instanceof ExternalServiceException) {
                throw (ExternalServiceException)e;
            }
            log.error("Error communicating with Account Service for user {}: {}", (Object)userId, (Object)e.getMessage());
            throw new ExternalServiceException("Error communicating with Account Service: " + e.getMessage(), e);
        }
    }

    private String normalizeBearerToken(String token) {
        if (token == null) {
            return "";
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}

