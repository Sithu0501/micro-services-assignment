package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.dto.FinalFareResponse;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.FareType;
import com.ridelink.fare_payment_service.exception.DuplicateFinalFareException;
import com.ridelink.fare_payment_service.repository.FareRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FareService {

    private static final BigDecimal BASE_FARE =
            new BigDecimal("100.00");

    private static final BigDecimal RATE_PER_KM =
            new BigDecimal("80.00");

    private final FareRepository fareRepository;
    private final FareCalculationService fareCalculationService;

    public FareService(
            FareRepository fareRepository,
            FareCalculationService fareCalculationService) {

        this.fareRepository = fareRepository;
        this.fareCalculationService = fareCalculationService;
    }

    public FinalFareResponse createFinalFare(FinalFareRequest request) {

        if (fareRepository
                .findByRideIdAndType(request.getRideId(), FareType.FINAL)
                .isPresent()) {

            throw new DuplicateFinalFareException(
                    "Final fare already exists for ride: "
                            + request.getRideId()
            );
        }

        BigDecimal amount =
                fareCalculationService
                        .calculateFareAmount(request.getDistanceKm());

        String fareId =
                "FAR-" + UUID.randomUUID();

        Fare fare = new Fare();

        fare.setId(fareId);
        fare.setRideId(request.getRideId());
        fare.setDistanceKm(
                BigDecimal.valueOf(request.getDistanceKm())
        );
        fare.setBaseFare(BASE_FARE);
        fare.setRatePerKm(RATE_PER_KM);
        fare.setAmount(amount);
        fare.setType(FareType.FINAL);
        fare.setCreatedAt(LocalDateTime.now());

        Fare savedFare =
                fareRepository.save(fare);

        return new FinalFareResponse(
                savedFare.getId(),
                savedFare.getRideId(),
                savedFare.getDistanceKm().doubleValue(),
                savedFare.getAmount(),
                "LKR",
                savedFare.getType().name()
        );
    }
}
