package com.ridelink.drivervehicleservice.util;

/**
 * Utility functions for geographic and distance computations.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {
        // Utility class
    }

    /**
     * Computes the great-circle distance between two GPS coordinates using the Haversine formula.
     *
     * @param lat1 latitude of first coordinate in degrees
     * @param lon1 longitude of first coordinate in degrees
     * @param lat2 latitude of second coordinate in degrees
     * @param lon2 longitude of second coordinate in degrees
     * @return distance in kilometers rounded to two decimal places
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double originLatRad = Math.toRadians(lat1);
        double targetLatRad = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0)
                * Math.cos(originLatRad) * Math.cos(targetLatRad);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = EARTH_RADIUS_KM * c;

        return Math.round(distance * 100.0) / 100.0;
    }
}
