package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RideService {

    private final DriverService driverService;
    private RideMatchingStrategy matchingStrategy;
    private FareStrategy fareStrategy;
    private final List<Ride> allRides = new ArrayList<>();

    /**
     * Spring injects the @Primary strategies via constructor.
     * They can be swapped at runtime using the setters below.
     */
    public RideService(DriverService driverService,
                       RideMatchingStrategy matchingStrategy,
                       FareStrategy fareStrategy) {
        this.driverService = driverService;
        this.matchingStrategy = matchingStrategy;
        this.fareStrategy = fareStrategy;
    }

    // ── Strategy Swapping (runtime) ──────────────────────────

    public void setMatchingStrategy(RideMatchingStrategy matchingStrategy) {
        this.matchingStrategy = matchingStrategy;
    }

    public void setFareStrategy(FareStrategy fareStrategy) {
        this.fareStrategy = fareStrategy;
    }

    public String getMatchingStrategyName() {
        return matchingStrategy.getClass().getSimpleName();
    }

    public String getFareStrategyName() {
        return fareStrategy.getClass().getSimpleName();
    }

    // ── Ride Life-Cycle ──────────────────────────────────────

    /**
     * Step 1 — Request a ride: creates the ride, matches a driver, and assigns.
     *
     * @return the ride with an assigned driver (status = ASSIGNED)
     * @throws NoDriverAvailableException if no suitable driver is found
     */
    public Ride requestRide(Rider rider,
                            double pickupLat, double pickupLon,
                            double dropOffLat, double dropOffLon) {

        // Delegate driver matching to the current strategy first, so a failed
        // match throws before any ride is created or recorded anywhere.
        List<Driver> availableDrivers = driverService.getAvailableDrivers();
        Driver matchedDriver = matchingStrategy.findDriver(rider, availableDrivers);

        Ride ride = new Ride(rider, pickupLat, pickupLon, dropOffLat, dropOffLon);

        // Transition: REQUESTED → ASSIGNED
        ride.assignDriver(matchedDriver);
        matchedDriver.setAvailable(false);

        // Record ride in both rider's and driver's history
        rider.addRide(ride);
        matchedDriver.addRide(ride);
        allRides.add(ride);

        return ride;
    }

    /**
     * Step 2 — Complete the ride: calculate fare and produce receipt.
     *
     * @return the FareReceipt for the completed ride
     */
    public FareReceipt completeRide(Ride ride) {
        // Delegate fare calculation to the current strategy
        double amount = fareStrategy.calculateFare(ride);

        // Build the receipt (Composition — owned by the ride)
        FareReceipt receipt = new FareReceipt(ride.getId(), amount);

        // Transition: ASSIGNED → COMPLETED (attaches the receipt)
        ride.completeRide(receipt);

        // Release the driver
        ride.getDriver().setAvailable(true);

        return receipt;
    }

    /**
     * Cancel a ride that has not yet been completed.
     */
    public void cancelRide(Ride ride) {
        // Release the driver if one was assigned
        if (ride.getDriver() != null) {
            ride.getDriver().setAvailable(true);
        }
        ride.cancelRide();
    }

    // ── Queries ──────────────────────────────────────────────

    public Optional<Ride> findById(String rideId) {
        return allRides.stream()
                .filter(r -> r.getId().equals(rideId))
                .findFirst();
    }

    /**
     * Returns an unmodifiable view of all rides in the system.
     */
    public List<Ride> getAllRides() {
        return Collections.unmodifiableList(allRides);
    }
}
