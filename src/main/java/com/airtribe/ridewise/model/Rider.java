package com.airtribe.ridewise.model;

import com.airtribe.ridewise.util.IdGenerator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Rider {

    private final String id;
    private final String name;
    private double latitude;
    private double longitude;
    private final List<Ride> rideHistory;

    public Rider(String name, double latitude, double longitude) {
        this.id = IdGenerator.nextRiderId();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rideHistory = new ArrayList<>();
    }

    public List<Ride> getRideHistory() {
        return Collections.unmodifiableList(rideHistory);
    }

    public int getTotalRides() {
        return rideHistory.size();
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addRide(Ride ride) {
        this.rideHistory.add(ride);
    }

    @Override
    public String toString() {
        return String.format("Rider{id='%s', name='%s', location=(%.4f, %.4f)}",
                id, name, latitude, longitude);
    }
}
