/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.mongodb.repository.MongoRepository
 *  org.springframework.stereotype.Repository
 */
package com.ridelink.ridemanagement.repository;

import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository
extends MongoRepository<Ride, String> {
    public Page<Ride> findByPassengerId(String var1, Pageable var2);

    public List<Ride> findByPassengerId(String var1);

    public Page<Ride> findByDriverId(String var1, Pageable var2);

    public List<Ride> findByDriverId(String var1);

    public Page<Ride> findByStatus(RideStatus var1, Pageable var2);

    public Page<Ride> findByPassengerIdAndStatus(String var1, RideStatus var2, Pageable var3);

    public Page<Ride> findByDriverIdAndStatus(String var1, RideStatus var2, Pageable var3);

    public Page<Ride> findByPassengerIdAndDriverId(String var1, String var2, Pageable var3);

    public Page<Ride> findByPassengerIdAndDriverIdAndStatus(String var1, String var2, RideStatus var3, Pageable var4);

    public boolean existsByPassengerIdAndStatusIn(String var1, Collection<RideStatus> var2);

    public boolean existsByDriverIdAndStatusIn(String var1, Collection<RideStatus> var2);

    public long countByStatus(RideStatus var1);
}

