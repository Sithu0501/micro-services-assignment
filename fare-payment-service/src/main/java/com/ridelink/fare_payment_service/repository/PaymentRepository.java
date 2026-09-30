package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.Payment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository
        extends MongoRepository<Payment, String> {

    boolean existsByRideId(String rideId);

    Optional<Payment> findByRideId(String rideId);
}