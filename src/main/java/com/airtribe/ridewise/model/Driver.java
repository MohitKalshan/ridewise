package com.airtribe.ridewise.model;

import com.airtribe.ridewise.util.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Driver {

    private final String id;
    private final String name;
    private double currentLatitude;
    private double currentLongitude;
    private boolean available;
    private final VehicleType vehicleType;
    private final List<Ride> rideHistory;

    public Driver(String name, double currentLatitude, double currentLongitude, VehicleType vehicleType) {
        this.id = IdGenerator.nextDriverId();
        this.name = name;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.available = true;
        this.vehicleType = vehicleType;
        this.rideHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public boolean isAvailable() {
        return available;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public List<Ride> getRideHistory() {
        return Collections.unmodifiableList(rideHistory);
    }
    
    public long getCompletedRideCount() {
        return rideHistory.stream()
                .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                .count();
    }

    public int getTotalRides() {
        return rideHistory.size();
    }

    public void updateLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void addRide(Ride ride) {
        this.rideHistory.add(ride);
    }

    @Override
    public String toString() {
        return String.format("Driver{id='%s', name='%s', vehicle=%s, location=(%.4f, %.4f), available=%s}",
                id, name, vehicleType, currentLatitude, currentLongitude, available);
    }
}
