package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.exception.ExternalServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class FareServiceClientTest {

    private MockRestServiceServer server;
    private FareServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://fare-payment.test");
        this.server = MockRestServiceServer.bindTo(builder).build();
        this.client = new FareServiceClient(builder.build());
    }

    @Test
    @DisplayName("Posts rideId and distanceKm to /api/fares/final")
    void postsFinalFareRequest() {
        this.server.expect(requestTo("http://fare-payment.test/api/fares/final"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rideId").value("ride123"))
                .andExpect(jsonPath("$.distanceKm").value(15.25))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("{}"));

        assertDoesNotThrow(() -> this.client.createFinalFare("ride123", 15.25));

        this.server.verify();
    }

    @Test
    @DisplayName("409 Conflict (final fare already exists) is treated as success")
    void conflictIsIdempotent() {
        this.server.expect(requestTo("http://fare-payment.test/api/fares/final"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertDoesNotThrow(() -> this.client.createFinalFare("ride123", 5.0));

        this.server.verify();
    }

    @Test
    @DisplayName("Other error responses surface as ExternalServiceException")
    void serverErrorIsWrapped() {
        this.server.expect(requestTo("http://fare-payment.test/api/fares/final"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ExternalServiceException.class, () -> this.client.createFinalFare("ride123", 5.0));
    }

    @Test
    @DisplayName("Forbidden responses surface as ExternalServiceException")
    void forbiddenIsWrapped() {
        this.server.expect(requestTo("http://fare-payment.test/api/fares/final"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThrows(ExternalServiceException.class, () -> this.client.createFinalFare("ride123", 5.0));
    }
}
