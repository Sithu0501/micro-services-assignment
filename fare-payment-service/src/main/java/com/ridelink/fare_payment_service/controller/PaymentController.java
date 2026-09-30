package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentResponse;
import com.ridelink.fare_payment_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(
        name = "Payment Management",
        description = "APIs for simulated ride payments"
)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(
            summary = "Create payment",
            description = "Creates a simulated payment using a saved final fare."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fare not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Payment already exists for this ride"
            )
    })
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response =
                paymentService.createPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment",
            description = "Returns payment details using the payment ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId) {

        PaymentResponse response =
                paymentService.getPayment(paymentId);

        return ResponseEntity.ok(response);
    }
}