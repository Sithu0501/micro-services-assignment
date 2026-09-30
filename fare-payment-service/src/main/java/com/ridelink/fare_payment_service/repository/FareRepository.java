package com.ridelink.fare_payment_service.repository;

import com.ridelink.fare_payment_service.entity.Fare;
import com.ridelink.fare_payment_service.entity.FareType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FareRepository extends MongoRepository<Fare, String> {

    Optional<Fare> findByRideIdAndType(
            String rideId,
            FareType type
    );
}