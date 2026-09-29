package com.ridelink.drivervehicleservice.repository;

import com.ridelink.drivervehicleservice.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Vehicle entities.
 */
@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    List<Vehicle> findByDriverId(String driverId);

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    void deleteByDriverId(String driverId);
}
