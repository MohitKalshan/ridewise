package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Primary
@Component("nearestDriverStrategy")
public class NearestDriverStrategy implements RideMatchingStrategy {

    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        return drivers.stream()
                .min(Comparator.comparingDouble(driver -> euclideanDistance(
                        rider.getLatitude(), rider.getLongitude(),
                        driver.getCurrentLatitude(), driver.getCurrentLongitude())))
                .orElseThrow(() -> new NoDriverAvailableException(
                        "No available drivers found near rider " + rider.getName()));
    }

    private double euclideanDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}
