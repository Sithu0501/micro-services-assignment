package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.ReceiptResponse;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.PaymentMethod;
import com.ridelink.fare_payment_service.entity.PaymentStatus;
import com.ridelink.fare_payment_service.exception.ReceiptNotFoundException;
import com.ridelink.fare_payment_service.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        receiptService = new ReceiptService(paymentRepository);
    }

    @Test
    void shouldGetReceiptSuccessfully() {

        Payment payment = new Payment();

        payment.setId("PAY-100");
        payment.setRideId("RIDE-100");
        payment.setFareId("FAR-100");
        payment.setAmount(new BigDecimal("900.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD_SIMULATED);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TXN-100");
        payment.setPaidAt(Instant.now());

        when(paymentRepository.findByRideId("RIDE-100"))
                .thenReturn(Optional.of(payment));

        ReceiptResponse response =
                receiptService.getReceiptByRideId("RIDE-100");

        assertNotNull(response);

        assertEquals(
                "RIDE-100",
                response.getRideId()
        );

        assertEquals(
                "FAR-100",
                response.getFareId()
        );

        assertEquals(
                "PAY-100",
                response.getPaymentId()
        );

        assertEquals(
                0,
                new BigDecimal("900.00")
                        .compareTo(response.getAmount())
        );

        assertEquals(
                "LKR",
                response.getCurrency()
        );

        assertEquals(
                PaymentMethod.CARD_SIMULATED,
                response.getPaymentMethod()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus()
        );

        assertEquals(
                "TXN-100",
                response.getTransactionReference()
        );

        assertNotNull(
                response.getPaidAt()
        );

        verify(paymentRepository)
                .findByRideId("RIDE-100");
    }

    @Test
    void shouldRejectUnknownRide() {

        when(paymentRepository.findByRideId("RIDE-NOT-FOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                ReceiptNotFoundException.class,
                () -> receiptService.getReceiptByRideId(
                        "RIDE-NOT-FOUND"
                )
        );
    }

    @Test
    void shouldRejectPaymentThatIsNotSuccessful() {

        Payment payment = new Payment();

        payment.setId("PAY-101");
        payment.setRideId("RIDE-101");
        payment.setFareId("FAR-101");
        payment.setAmount(new BigDecimal("900.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.CARD_SIMULATED);

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        payment.setTransactionReference("TXN-101");

        when(paymentRepository.findByRideId("RIDE-101"))
                .thenReturn(Optional.of(payment));

        assertThrows(
                ReceiptNotFoundException.class,
                () -> receiptService.getReceiptByRideId(
                        "RIDE-101"
                )
        );
    }
}