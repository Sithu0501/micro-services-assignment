package com.ridelink.fare_payment_service.controller;

import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.service.ReceiptService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipts")
@Tag(
        name = "Receipt Management",
        description = "APIs for retrieving ride payment receipts"
)
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(
            ReceiptService receiptService) {

        this.receiptService = receiptService;
    }

    @GetMapping("/{rideId}")
    @Operation(
            summary = "Get receipt",
            description = "Returns the payment receipt for a completed ride."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Receipt retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Receipt not found"
            )
    })
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable String rideId) {

        ReceiptResponse response =
                receiptService.getReceiptByRideId(rideId);

        return ResponseEntity.ok(response);
    }
}