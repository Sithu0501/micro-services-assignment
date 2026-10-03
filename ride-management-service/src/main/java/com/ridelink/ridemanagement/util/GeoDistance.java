package com.ridelink.ridemanagement.util;

import com.ridelink.ridemanagement.model.Location;

import java.util.Optional;

/**
 * Simulated trip-distance calculation.
 *
 * <p>RideLink uses fictional, simulated locations, so the trip distance is derived from the
 * pickup and destination coordinates using the Haversine great-circle formula.
 */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoDistance() {
    }

    /**
     * @return the great-circle distance in kilometres rounded to two decimals, or empty when
     *         either location (or one of its coordinates) is missing.
     */
    public static Optional<Double> kilometres(Location from, Location to) {
        if (from == null || to == null
                || from.getLatitude() == null || from.getLongitude() == null
                || to.getLatitude() == null || to.getLongitude() == null) {
            return Optional.empty();
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double distance = EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Optional.of(Math.round(distance * 100.0) / 100.0);
    }
}
