package com.ridelink.drivervehicleservice.repository;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Driver entities.
 */
@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);

    List<Driver> findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus availabilityStatus, String serviceArea);
}
