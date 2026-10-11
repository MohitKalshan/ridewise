package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("defaultFareStrategy")
public class DefaultFareStrategy implements FareStrategy {

    private static final double BASE_FARE = 50.0;
    private static final double PER_KM_RATE = 10.0;

    @Override
    public double calculateFare(Ride ride) {
        return BASE_FARE + (ride.getDistance() * PER_KM_RATE);
    }
}
