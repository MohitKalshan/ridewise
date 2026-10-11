# RideWise — Class Model

## Class Diagram

```mermaid
classDiagram
    class Rider {
        -String id
        -String name
        -double latitude
        -double longitude
        -List~Ride~ rideHistory
        +getId() String
        +getName() String
        +getLatitude() double
        +getLongitude() double
        +getRideHistory() List~Ride~
        +getTotalRides() int
        +updateLocation(lat, lon)
        +addRide(Ride)
    }

    class Driver {
        -String id
        -String name
        -double currentLatitude
        -double currentLongitude
        -boolean available
        -VehicleType vehicleType
        -List~Ride~ rideHistory
        +getId() String
        +getName() String
        +getCurrentLatitude() double
        +getCurrentLongitude() double
        +isAvailable() boolean
        +getVehicleType() VehicleType
        +getCompletedRideCount() long
        +getTotalRides() int
        +setAvailable(boolean)
        +updateLocation(lat, lon)
        +addRide(Ride)
    }

    class Ride {
        -String id
        -Rider rider
        -Driver driver
        -double pickupLatitude
        -double pickupLongitude
        -double dropOffLatitude
        -double dropOffLongitude
        -double distance
        -RideStatus status
        -FareReceipt fareReceipt
        +getId() String
        +getRider() Rider
        +getDriver() Driver
        +getDistance() double
        +getStatus() RideStatus
        +getFareReceipt() FareReceipt
        +assignDriver(Driver)
        +completeRide(FareReceipt)
        +cancelRide()
    }

    class FareReceipt {
        -String rideId
        -double amount
        -LocalDateTime generatedAt
        +getRideId() String
        +getAmount() double
        +getGeneratedAt() LocalDateTime
    }

    class RideStatus {
        <<enumeration>>
        REQUESTED
        ASSIGNED
        COMPLETED
        CANCELLED
    }

    class VehicleType {
        <<enumeration>>
        BIKE
        AUTO
        CAR
    }

    class RideMatchingStrategy {
        <<interface>>
        +findDriver(Rider, List~Driver~) Driver
    }

    class NearestDriverStrategy {
        +findDriver(Rider, List~Driver~) Driver
        -euclideanDistance(lat1, lon1, lat2, lon2) double
    }

    class LeastActiveDriverStrategy {
        +findDriver(Rider, List~Driver~) Driver
    }

    class FareStrategy {
        <<interface>>
        +calculateFare(Ride) double
    }

    class DefaultFareStrategy {
        -double BASE_FARE = 50.0
        -double PER_KM_RATE = 10.0
        +calculateFare(Ride) double
    }

    class PeakHourFareStrategy {
        -double BASE_FARE = 50.0
        -double PER_KM_RATE = 10.0
        -double PEAK_MULTIPLIER = 1.5
        +calculateFare(Ride) double
    }

    class RideService {
        -DriverService driverService
        -RideMatchingStrategy matchingStrategy
        -FareStrategy fareStrategy
        -List~Ride~ allRides
        +requestRide(Rider, pickupLat, pickupLon, dropLat, dropLon) Ride
        +completeRide(Ride) FareReceipt
        +cancelRide(Ride)
        +setMatchingStrategy(RideMatchingStrategy)
        +setFareStrategy(FareStrategy)
        +findById(String) Optional~Ride~
        +getAllRides() List~Ride~
    }

    class RiderService {
        -List~Rider~ riders
        +registerRider(name, lat, lon) Rider
        +findById(String) Optional~Rider~
        +getAllRiders() List~Rider~
    }

    class DriverService {
        -List~Driver~ drivers
        +registerDriver(name, lat, lon, vehicleType) Driver
        +findById(String) Optional~Driver~
        +updateAvailability(driverId, available)
        +getAvailableDrivers() List~Driver~
        +getAllDrivers() List~Driver~
    }

    class NoDriverAvailableException {
        +NoDriverAvailableException(String message)
    }

    class IdGenerator {
        -AtomicLong riderCounter$
        -AtomicLong driverCounter$
        -AtomicLong rideCounter$
        +nextRiderId()$ String
        +nextDriverId()$ String
        +nextRideId()$ String
    }

    %% Relationships
    Ride --> Rider : rider (Association)
    Ride --> Driver : driver (Association)
    Ride *-- FareReceipt : fareReceipt (Composition)
    Ride --> RideStatus : status
    Driver --> VehicleType : vehicleType
    Rider "1" --> "*" Ride : rideHistory (Association)
    Driver "1" --> "*" Ride : rideHistory (Association)

    RideMatchingStrategy <|.. NearestDriverStrategy : implements
    RideMatchingStrategy <|.. LeastActiveDriverStrategy : implements
    FareStrategy <|.. DefaultFareStrategy : implements
    FareStrategy <|.. PeakHourFareStrategy : implements

    RideService --> DriverService : depends on
    RideService o-- RideMatchingStrategy : composed (Composition)
    RideService o-- FareStrategy : composed (Composition)

    NearestDriverStrategy ..> NoDriverAvailableException : throws
    LeastActiveDriverStrategy ..> NoDriverAvailableException : throws
```

---

## Entity Summary

| Entity        | Package   | Type        | Purpose                                    |
|---------------|-----------|-------------|--------------------------------------------|
| `Rider`       | `model`   | Class       | Represents a passenger with ride history   |
| `Driver`      | `model`   | Class       | Represents a driver with availability and vehicle type |
| `Ride`        | `model`   | Class       | Represents a trip with validated state machine |
| `FareReceipt` | `model`   | Value Object| Immutable fare breakdown for a completed ride |
| `RideStatus`  | `model`   | Enum        | Life-cycle states: REQUESTED → ASSIGNED → COMPLETED / CANCELLED |
| `VehicleType` | `model`   | Enum        | BIKE, AUTO, CAR                            |

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| `FareReceipt` is immutable | Value object — once calculated, a receipt never changes |
| `FareStrategy` returns `double`, not `FareReceipt` | Strategy is purely about pricing logic; the service builds the receipt (SRP) |
| `Ride` validates its own state transitions | High cohesion — the entity that owns the state enforces its rules |
| Strategies are interfaces, not abstract classes | Composition over inheritance — allows multiple independent strategy dimensions |
| `RideService` takes strategies via constructor | Constructor injection (Spring DI) enables testability and explicit dependencies |
| `VehicleType` is an enum on `Driver` | Extensible for future fare logic without modifying existing strategies (OCP) |
| `IdGenerator` uses `AtomicLong` | Thread-safe ID generation without synchronization overhead |
| Ride history returns unmodifiable lists | Prevents external mutation of internal state (Law of Demeter) |
