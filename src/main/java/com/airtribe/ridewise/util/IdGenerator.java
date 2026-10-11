package com.airtribe.ridewise.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong riderCounter = new AtomicLong(0);
    private static final AtomicLong driverCounter = new AtomicLong(0);
    private static final AtomicLong rideCounter = new AtomicLong(0);

    private IdGenerator() {
    }

    public static String nextRiderId() {
        return "RIDER-" + riderCounter.incrementAndGet();
    }

    public static String nextDriverId() {
        return "DRIVER-" + driverCounter.incrementAndGet();
    }

    public static String nextRideId() {
        return "RIDE-" + rideCounter.incrementAndGet();
    }
}
