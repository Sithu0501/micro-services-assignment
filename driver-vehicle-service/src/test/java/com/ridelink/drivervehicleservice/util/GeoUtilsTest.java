package com.ridelink.drivervehicleservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GeoUtils Unit Tests")
class GeoUtilsTest {

    @Test
    @DisplayName("Should return 0.0 for identical coordinates")
    void testSameCoordinates() {
        double dist = GeoUtils.calculateDistanceKm(6.9271, 79.8612, 6.9271, 79.8612);
        assertEquals(0.0, dist);
    }

    @Test
    @DisplayName("Should accurately calculate distance between Colombo and Kandy (~95-100 km)")
    void testColomboToKandyDistance() {
        // Colombo: 6.9271, 79.8612
        // Kandy: 7.2906, 80.6337
        double dist = GeoUtils.calculateDistanceKm(6.9271, 79.8612, 7.2906, 80.6337);
        assertTrue(dist > 90.0 && dist < 110.0, "Expected distance around 95-100km, got: " + dist);
    }

    @Test
    @DisplayName("Should calculate short distance within city (< 5 km)")
    void testShortCityDistance() {
        // Two points ~2km apart in Colombo
        double dist = GeoUtils.calculateDistanceKm(6.9271, 79.8612, 6.9350, 79.8700);
        assertTrue(dist > 1.0 && dist < 3.0, "Expected distance around 1.5-2km, got: " + dist);
    }
}
