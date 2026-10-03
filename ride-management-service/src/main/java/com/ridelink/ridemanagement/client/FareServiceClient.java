package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.exception.ExternalServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * Synchronous REST client for the Fare &amp; Payment Service.
 *
 * <p>When a ride is completed, Ride Management asks Fare &amp; Payment to calculate and persist
 * the final fare for that ride. The caller's bearer token is forwarded so the downstream
 * service applies its own role-based authorisation.
 */
@Component
public class FareServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FareServiceClient.class);

    private final RestClient paymentServiceRestClient;

    public FareServiceClient(@Qualifier("paymentServiceRestClient") RestClient paymentServiceRestClient) {
        this.paymentServiceRestClient = paymentServiceRestClient;
    }

    /**
     * Requests the final fare for a completed ride. A {@code 409 Conflict} (final fare already
     * exists) is treated as success so the call is idempotent.
     *
     * @throws ExternalServiceException if Fare &amp; Payment is unreachable or answers with an error
     */
    public void createFinalFare(String rideId, double distanceKm) {
        try {
            RestClient.RequestBodySpec request = this.paymentServiceRestClient.post()
                    .uri("/api/fares/final")
                    .contentType(MediaType.APPLICATION_JSON);

            String authorization = currentAuthorizationHeader();
            if (authorization != null) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorization);
            }

            request.body(Map.of("rideId", rideId, "distanceKm", distanceKm))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Final fare requested from Fare & Payment Service for ride {} ({} km)", rideId, distanceKm);
        } catch (HttpClientErrorException.Conflict e) {
            log.info("Final fare already exists for ride {}; nothing to do", rideId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException(
                    "Fare & Payment Service is currently unavailable. Please try again later.", e);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                    "Fare & Payment Service rejected the final fare request: " + e.getStatusCode(), e);
        }
    }

    private String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
