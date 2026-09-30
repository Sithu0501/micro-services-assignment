package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.FareEstimateRequest;
import com.ridelink.fare_payment_service.dto.FareEstimateResponse;
import com.ridelink.fare_payment_service.dto.FinalFareRequest;
import com.ridelink.fare_payment_service.dto.FinalFareResponse;
import com.ridelink.fare_payment_service.service.FareCalculationService;
import com.ridelink.fare_payment_service.service.FareService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
@Tag(
        name = "Fare Management",
        description = "APIs for fare estimation and final fare calculation"
)
public class FareController {

    private final FareCalculationService fareCalculationService;
    private final FareService fareService;

    public FareController(
            FareCalculationService fareCalculationService,
            FareService fareService) {

        this.fareCalculationService = fareCalculationService;
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @Operation(
            summary = "Estimate fare",
            description = "Calculates an estimated fare using the supplied distance."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pickup, destination or distance"
            )
    })
    public ResponseEntity<FareEstimateResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {

        FareEstimateResponse response =
                fareCalculationService.calculateEstimate(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/final")
    @Operation(
            summary = "Calculate final fare",
            description = "Calculates and saves the final fare for a completed ride."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Final fare calculated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ride ID or distance"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Final fare already exists for this ride"
            )
    })
    public ResponseEntity<FinalFareResponse> calculateFinalFare(
            @Valid @RequestBody FinalFareRequest request) {

        FinalFareResponse response =
                fareService.createFinalFare(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}