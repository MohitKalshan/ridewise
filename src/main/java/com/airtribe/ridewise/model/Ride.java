package com.airtribe.ridewise.model;

import com.airtribe.ridewise.util.IdGenerator;

public class Ride {

    private final String id;
    private final Rider rider;
    private Driver driver;
    private final double pickupLatitude;
    private final double pickupLongitude;
    private final double dropOffLatitude;
    private final double dropOffLongitude;
    private final double distance;
    private RideStatus status;
    private FareReceipt fareReceipt;  // Composition — owned by this Ride

    public Ride(Rider rider, double pickupLatitude, double pickupLongitude,
                double dropOffLatitude, double dropOffLongitude) {
        this.id = IdGenerator.nextRideId();
        this.rider = rider;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropOffLatitude = dropOffLatitude;
        this.dropOffLongitude = dropOffLongitude;
        this.distance = calculateDistance(pickupLatitude, pickupLongitude, dropOffLatitude, dropOffLongitude);
        this.status = RideStatus.REQUESTED;
    }

    public String getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public double getDropOffLatitude() {
        return dropOffLatitude;
    }

    public double getDropOffLongitude() {
        return dropOffLongitude;
    }

    public double getDistance() {
        return distance;
    }

    public RideStatus getStatus() {
        return status;
    }

    public FareReceipt getFareReceipt() {
        return fareReceipt;
    }

    public void assignDriver(Driver driver) {
        if (this.status != RideStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Cannot assign driver — ride is in " + status + " state (expected REQUESTED)");
        }
        this.driver = driver;
        this.status = RideStatus.ASSIGNED;
    }

    public void completeRide(FareReceipt fareReceipt) {
        if (this.status != RideStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Cannot complete ride — ride is in " + status + " state (expected ASSIGNED)");
        }
        this.fareReceipt = fareReceipt;
        this.status = RideStatus.COMPLETED;
    }

    public void cancelRide() {
        if (this.status == RideStatus.COMPLETED || this.status == RideStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot cancel ride — ride is in " + status + " state");
        }
        this.status = RideStatus.CANCELLED;
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public String toString() {
        return String.format(
                "Ride{id='%s', rider='%s', driver='%s', distance=%.2f km, status=%s, fare=%s}",
                id,
                rider.getName(),
                driver != null ? driver.getName() : "UNASSIGNED",
                distance,
                status,
                fareReceipt != null ? String.format("₹%.2f", fareReceipt.getAmount()) : "N/A");
    }
}
