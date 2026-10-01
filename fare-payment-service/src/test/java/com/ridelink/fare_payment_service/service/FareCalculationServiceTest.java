package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.dto.FareEstimateResponse;
import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.dto.FinalFareResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculationServiceTest {

    private final FareCalculationService fareCalculationService =
            new FareCalculationService();

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldCalculateFareForTenKilometers() {

        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickup("Beliatta");
        request.setDestination("Matara");
        request.setDistanceKm(10.0);

        FareEstimateResponse response =
                fareCalculationService.calculateEstimate(request);

        assertEquals(
                new BigDecimal("900.00"),
                response.getAmount()
        );

        assertEquals("LKR", response.getCurrency());
        assertEquals("ESTIMATE", response.getType());
    }

    @Test
    void shouldReturnBaseFareForZeroDistance() {

        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickup("Beliatta");
        request.setDestination("Beliatta Town");
        request.setDistanceKm(0.0);

        FareEstimateResponse response =
                fareCalculationService.calculateEstimate(request);

        assertEquals(
                new BigDecimal("100.00"),
                response.getAmount()
        );
    }

    @Test
    void shouldRejectNegativeDistance() {

        FareEstimateRequest request = new FareEstimateRequest();
        request.setPickup("Beliatta");
        request.setDestination("Matara");
        request.setDistanceKm(-5.0);

        Set<ConstraintViolation<FareEstimateRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(v ->
                                v.getMessage()
                                        .equals("Distance cannot be negative"))
        );
    }

    @Test
void shouldCalculateFinalFare() {

    FinalFareRequest request = new FinalFareRequest();
    request.setRideId("ride-12345");
    request.setDistanceKm(10.0);

    FinalFareResponse response =
            fareCalculationService.calculateFinalFare(request);

    assertNotNull(response.getFareId());
    assertEquals("ride-12345", response.getRideId());
    assertEquals(
            new BigDecimal("900.00"),
            response.getAmount()
    );
    assertEquals("LKR", response.getCurrency());
    assertEquals("FINAL", response.getFareType());
}

@Test
void shouldRejectNegativeDistanceForFinalFare() {

    FinalFareRequest request = new FinalFareRequest();
    request.setRideId("ride-12345");
    request.setDistanceKm(-5.0);

    Set<ConstraintViolation<FinalFareRequest>> violations =
            validator.validate(request);

    assertFalse(violations.isEmpty());

    assertTrue(
            violations.stream()
                    .anyMatch(v ->
                            v.getMessage()
                                    .equals("Distance cannot be negative"))
    );
}
    
}