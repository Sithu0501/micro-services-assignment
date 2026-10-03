package com.ridelink.ridemanagement.util;

import com.ridelink.ridemanagement.model.Location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoDistanceTest {

    @Test
    @DisplayName("Distance between identical points is zero")
    void samePointIsZero() {
        Location point = new Location("Fort", 6.9344, 79.8428);

        assertEquals(0.0, GeoDistance.kilometres(point, point).orElseThrow());
    }

    @Test
    @DisplayName("SLIIT Malabe to Fort is roughly 15 km")
    void realisticSimulatedTrip() {
        Location pickup = new Location("SLIIT Malabe", 6.9147, 79.9729);
        Location destination = new Location("Fort", 6.9344, 79.8428);

        double km = GeoDistance.kilometres(pickup, destination).orElseThrow();

        assertTrue(km > 14.0 && km < 16.0, "unexpected distance: " + km);
    }

    @Test
    @DisplayName("Distance is symmetric")
    void symmetric() {
        Location a = new Location("A", 6.9, 79.9);
        Location b = new Location("B", 7.2, 80.1);

        assertEquals(
                GeoDistance.kilometres(a, b).orElseThrow(),
                GeoDistance.kilometres(b, a).orElseThrow());
    }

    @Test
    @DisplayName("Missing locations or coordinates produce an empty result")
    void missingCoordinates() {
        Location complete = new Location("A", 6.9, 79.9);

        assertTrue(GeoDistance.kilometres(null, complete).isEmpty());
        assertTrue(GeoDistance.kilometres(complete, null).isEmpty());
        assertTrue(GeoDistance.kilometres(new Location("B", null, 79.9), complete).isEmpty());
        assertTrue(GeoDistance.kilometres(complete, new Location("C", 6.9, null)).isEmpty());
    }
}
