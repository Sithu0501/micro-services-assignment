package com.ridelink.fare_payment_service.service;

import com.ridelink.fare_payment_service.dto.PaymentRequest;
import com.ridelink.fare_payment_service.dto.PaymentResponse;
import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.FareType;
import com.ridelink.fare_payment_service.entity.Payment;
import com.ridelink.fare_payment_service.entity.PaymentMethod;
import com.ridelink.fare_payment_service.entity.PaymentStatus;
import com.ridelink.fare_payment_service.exception.DuplicatePaymentException;
import com.ridelink.fare_payment_service.exception.FareNotFoundException;
import com.ridelink.fare_payment_service.exception.PaymentNotFoundException;
import com.ridelink.fare_payment_service.repository.FareRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FareRepository fareRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {

        paymentService = new PaymentService(
                paymentRepository,
                fareRepository
        );
    }

    @Test
    void shouldCreatePaymentSuccessfully() {

        String rideId = "RIDE-100";
        String fareId = "FAR-100";

        PaymentRequest request = new PaymentRequest();
        request.setRideId(rideId);
        request.setFareId(fareId);
        request.setPaymentMethod(PaymentMethod.CARD_SIMULATED);

        Fare fare = createFinalFare(
                fareId,
                rideId,
                new BigDecimal("900.00")
        );

        when(paymentRepository.existsByRideId(rideId))
                .thenReturn(false);

        when(fareRepository.findById(fareId))
                .thenReturn(Optional.of(fare));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.createPayment(request);

        assertNotNull(response);

        assertTrue(
                response.getPaymentId().startsWith("PAY-")
        );

        assertEquals(
                rideId,
                response.getRideId()
        );

        assertEquals(
                fareId,
                response.getFareId()
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

        assertTrue(
                response.getTransactionReference()
                        .startsWith("TXN-")
        );

        assertNotNull(
                response.getPaidAt()
        );

        verify(paymentRepository)
                .save(any(Payment.class));
    }

    @Test
    void shouldRejectDuplicatePayment() {

        PaymentRequest request = new PaymentRequest();

        request.setRideId("RIDE-101");
        request.setFareId("FAR-101");
        request.setPaymentMethod(
                PaymentMethod.CARD_SIMULATED
        );

        when(paymentRepository.existsByRideId("RIDE-101"))
                .thenReturn(true);

        assertThrows(
                DuplicatePaymentException.class,
                () -> paymentService.createPayment(request)
        );

        verify(fareRepository, never())
                .findById(anyString());

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldRejectUnknownFare() {

        PaymentRequest request = new PaymentRequest();

        request.setRideId("RIDE-102");
        request.setFareId("FAR-NOT-FOUND");
        request.setPaymentMethod(
                PaymentMethod.CARD_SIMULATED
        );

        when(paymentRepository.existsByRideId("RIDE-102"))
                .thenReturn(false);

        when(fareRepository.findById("FAR-NOT-FOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                FareNotFoundException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldRejectFareRideMismatch() {

        PaymentRequest request = new PaymentRequest();

        request.setRideId("RIDE-103");
        request.setFareId("FAR-103");
        request.setPaymentMethod(
                PaymentMethod.CARD_SIMULATED
        );

        Fare fare = createFinalFare(
                "FAR-103",
                "RIDE-DIFFERENT",
                new BigDecimal("900.00")
        );

        when(paymentRepository.existsByRideId("RIDE-103"))
                .thenReturn(false);

        when(fareRepository.findById("FAR-103"))
                .thenReturn(Optional.of(fare));

        assertThrows(
                FareNotFoundException.class,
                () -> paymentService.createPayment(request)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void shouldGetPaymentSuccessfully() {

        Payment payment = new Payment();

        payment.setId("PAY-100");
        payment.setRideId("RIDE-100");
        payment.setFareId("FAR-100");
        payment.setAmount(
                new BigDecimal("900.00")
        );
        payment.setCurrency("LKR");
        payment.setPaymentMethod(
                PaymentMethod.CARD_SIMULATED
        );
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );
        payment.setTransactionReference(
                "TXN-100"
        );
        payment.setPaidAt(
                Instant.now()
        );

        when(paymentRepository.findById("PAY-100"))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.getPayment("PAY-100");

        assertNotNull(response);

        assertEquals(
                "PAY-100",
                response.getPaymentId()
        );

        assertEquals(
                "RIDE-100",
                response.getRideId()
        );

        assertEquals(
                "FAR-100",
                response.getFareId()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus()
        );
    }

    @Test
    void shouldRejectUnknownPaymentId() {

        when(paymentRepository.findById("PAY-NOT-FOUND"))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPayment(
                        "PAY-NOT-FOUND"
                )
        );
    }

    private Fare createFinalFare(
            String fareId,
            String rideId,
            BigDecimal amount) {

        Fare fare = new Fare();

        fare.setId(fareId);
        fare.setRideId(rideId);
        fare.setDistanceKm(
                new BigDecimal("10.00")
        );
        fare.setBaseFare(
                new BigDecimal("100.00")
        );
        fare.setRatePerKm(
                new BigDecimal("80.00")
        );
        fare.setAmount(amount);
        fare.setType(FareType.FINAL);

        return fare;
    }
}