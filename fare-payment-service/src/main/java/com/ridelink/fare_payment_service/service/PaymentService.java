package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentResponse;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.FareType;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.PaymentStatus;
import com.ridelink.fare_payment_service.exception.DuplicatePaymentException;
import com.ridelink.fare_payment_service.exception.FareNotFoundException;
import com.ridelink.fare_payment_service.exception.PaymentNotFoundException;
import com.ridelink.fare_payment_service.repository.FareRepository;
import com.ridelink.fare_payment_service.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareRepository fareRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            FareRepository fareRepository) {

        this.paymentRepository = paymentRepository;
        this.fareRepository = fareRepository;
    }

    public PaymentResponse createPayment(PaymentRequest request) {

        if (paymentRepository.existsByRideId(request.getRideId())) {
            throw new DuplicatePaymentException(
                    "Payment already exists for ride: "
                            + request.getRideId()
            );
        }

        Fare fare = fareRepository
                .findById(request.getFareId())
                .orElseThrow(() ->
                        new FareNotFoundException(
                                "Fare not found: "
                                        + request.getFareId()
                        )
                );

        if (fare.getType() != FareType.FINAL) {
            throw new FareNotFoundException(
                    "Final fare not found for fare ID: "
                            + request.getFareId()
            );
        }

        if (!fare.getRideId().equals(request.getRideId())) {
            throw new FareNotFoundException(
                    "Fare does not belong to ride: "
                            + request.getRideId()
            );
        }

        Payment payment = new Payment();

        payment.setId(
                "PAY-" + UUID.randomUUID()
        );

        payment.setRideId(
                fare.getRideId()
        );

        payment.setFareId(
                fare.getId()
        );

        payment.setAmount(
                fare.getAmount()
        );

        payment.setCurrency(
                "LKR"
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID()
        );

        payment.setPaidAt(
                Instant.now()
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    public PaymentResponse getPayment(String paymentId) {

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found: "
                                        + paymentId
                        )
                );

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getFareId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionReference(),
                payment.getPaidAt()
        );
    }
}