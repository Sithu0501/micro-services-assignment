package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.PaymentStatus;
import com.ridelink.fare_payment_service.exception.ReceiptNotFoundException;
import com.ridelink.fare_payment_service.repository.PaymentRepository;

import org.springframework.stereotype.Service;

@Service
public class ReceiptService {

    private final PaymentRepository paymentRepository;

    public ReceiptService(
            PaymentRepository paymentRepository) {

        this.paymentRepository = paymentRepository;
    }

    public ReceiptResponse getReceiptByRideId(String rideId) {

        Payment payment = paymentRepository
                .findByRideId(rideId)
                .orElseThrow(() ->
                        new ReceiptNotFoundException(
                                "Receipt not found for ride: " + rideId
                        )
                );

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new ReceiptNotFoundException(
                    "Successful payment not found for ride: " + rideId
            );
        }

        return new ReceiptResponse(
                payment.getRideId(),
                payment.getFareId(),
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionReference(),
                payment.getPaidAt()
        );
    }
}