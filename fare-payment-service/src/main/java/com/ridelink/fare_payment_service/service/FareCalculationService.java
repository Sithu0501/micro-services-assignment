package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.dto.FareEstimateResponse;
import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.dto.FinalFareResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class FareCalculationService {

    private static final BigDecimal BASE_FARE =
            new BigDecimal("100.00");

    private static final BigDecimal RATE_PER_KM =
            new BigDecimal("80.00");

    public BigDecimal calculateFareAmount(Double distanceKm) {

        BigDecimal distance =
                BigDecimal.valueOf(distanceKm);

        return BASE_FARE
                .add(distance.multiply(RATE_PER_KM))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public FareEstimateResponse calculateEstimate(
            FareEstimateRequest request) {

        BigDecimal totalFare =
                calculateFareAmount(request.getDistanceKm());

        return new FareEstimateResponse(
                request.getPickup(),
                request.getDestination(),
                request.getDistanceKm(),
                BASE_FARE,
                RATE_PER_KM,
                totalFare,
                "LKR",
                "ESTIMATE"
        );
    }

    public FinalFareResponse calculateFinalFare(
            FinalFareRequest request) {

        BigDecimal totalFare =
                calculateFareAmount(request.getDistanceKm());

        String fareId = UUID.randomUUID().toString();

        return new FinalFareResponse(
                fareId,
                request.getRideId(),
                request.getDistanceKm(),
                totalFare,
                "LKR",
                "FINAL"
        );
    }
}