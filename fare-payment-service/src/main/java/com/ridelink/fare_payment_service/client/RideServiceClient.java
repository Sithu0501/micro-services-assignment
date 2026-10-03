package com.ridelink.fare_payment_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ridelink.fare_payment_service.exception.PaymentNotAllowedException;
import com.ridelink.fare_payment_service.exception.RideNotCompletedException;
import com.ridelink.fare_payment_service.exception.RideNotFoundException;
import com.ridelink.fare_payment_service.exception.RideServiceUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Synchronous REST client for the Ride Management Service.
 *
 * <p>Before a payment is accepted, Fare &amp; Payment confirms with Ride Management that the
 * ride exists, is visible to the caller and has reached the {@code COMPLETED} state. The
 * caller's bearer token is forwarded so Ride Management applies its own ownership rules
 * (a passenger can only pay for their own ride).
 */
@Component
public class RideServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RideServiceClient.class);

    static final String COMPLETED = "COMPLETED";

    private final RestClient rideServiceRestClient;

    public RideServiceClient(@Qualifier("rideServiceRestClient") RestClient rideServiceRestClient) {
        this.rideServiceRestClient = rideServiceRestClient;
    }

    /**
     * @throws RideNotFoundException            the ride does not exist
     * @throws PaymentNotAllowedException       the caller may not access the ride
     * @throws RideNotCompletedException        the ride is not COMPLETED yet
     * @throws RideServiceUnavailableException  Ride Management is unreachable or failing
     */
    public void verifyRideCompleted(String rideId) {
        RideEnvelope envelope;

        try {
            RestClient.RequestHeadersSpec<?> request = this.rideServiceRestClient.get()
                    .uri("/api/v1/rides/{id}", rideId);

            String authorization = currentAuthorizationHeader();
            if (authorization != null) {
                request = request.header(HttpHeaders.AUTHORIZATION, authorization);
            }

            envelope = request.retrieve().body(RideEnvelope.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RideNotFoundException("Ride not found: " + rideId);
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            throw new PaymentNotAllowedException("You are not allowed to pay for ride: " + rideId);
        } catch (RestClientException e) {
            log.error("Ride Management Service call failed for ride {}: {}", rideId, e.getMessage());
            throw new RideServiceUnavailableException(
                    "Ride Management Service is currently unavailable. Please try again later.", e);
        }

        String status = envelope != null && envelope.data() != null ? envelope.data().status() : null;

        if (!COMPLETED.equals(status)) {
            throw new RideNotCompletedException(
                    "Payment is only allowed for COMPLETED rides. Ride " + rideId
                            + " is currently: " + (status == null ? "UNKNOWN" : status));
        }
    }

    private String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RideEnvelope(RideData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RideData(String status) {
    }
}
