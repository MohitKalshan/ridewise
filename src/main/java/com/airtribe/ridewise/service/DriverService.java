package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.VehicleType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final List<Driver> drivers = new ArrayList<>();

    public Driver registerDriver(String name, double latitude, double longitude, VehicleType vehicleType) {
        Driver driver = new Driver(name, latitude, longitude, vehicleType);
        drivers.add(driver);
        return driver;
    }

    public Optional<Driver> findById(String driverId) {
        return drivers.stream()
                .filter(d -> d.getId().equals(driverId))
                .findFirst();
    }

    public void updateAvailability(String driverId, boolean available) {
        findById(driverId).ifPresent(d -> d.setAvailable(available));
    }

    public List<Driver> getAvailableDrivers() {
        return drivers.stream()
                .filter(Driver::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Driver> getAllDrivers() {
        return Collections.unmodifiableList(drivers);
    }
}
