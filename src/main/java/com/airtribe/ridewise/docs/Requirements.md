# RideWise — Requirements

## Overview

RideWise is a console-based Ride-Sharing (Uber/Ola style) System built with **Spring Boot**.  
The goal is to demonstrate high-quality **Low-Level Design (LLD)** and design thinking — not to build a production-ready app.

---

## Learning Objectives

### OOP & SOLID
| Principle | How It's Applied |
|-----------|-----------------|
| **SRP** | Single-purpose classes — `RideService`, `DriverService`, `RiderService`, each strategy |
| **OCP** | New ride-selection or pricing strategies added without modifying core logic |
| **LSP** | Any strategy implementation is fully interchangeable |
| **ISP** | Small focused interfaces — `RideMatchingStrategy`, `FareStrategy` |
| **DIP** | Services depend on interfaces, not concrete classes |

### Design Principles
| Principle | How It's Applied |
|-----------|-----------------|
| **DRY** | No duplication in ride allocation logic — centralized in `RideService` |
| **KISS** | Simple entity modeling with minimal fields |
| **YAGNI** | MVP feature set — only what's needed |
| **Law of Demeter** | Services communicate with direct collaborators — no deep method chains |

---

## A. Functional Requirements

### FR-1: Register Riders
- Register riders with **name** and **GPS location** (latitude, longitude)
- Each rider receives a unique auto-generated ID (`RIDER-1`, `RIDER-2`, ...)

### FR-2: Register Drivers
- Register drivers with **name**, **GPS location**, and **vehicle type** (BIKE, AUTO, CAR)
- Each driver receives a unique auto-generated ID (`DRIVER-1`, `DRIVER-2`, ...)
- Drivers start as **available** by default

### FR-3: Show Available Drivers
- List all drivers currently marked as available
- Availability is automatically toggled on ride assignment and completion

### FR-4: Request a Ride
- A rider provides **pickup** and **drop-off** coordinates
- System matches a driver using the active `RideMatchingStrategy`
- Distance is calculated via the **Haversine formula**
- Ride is created with status `ASSIGNED`

### FR-5: Match Ride to Driver (pluggable strategies)
- **NearestDriverStrategy** — matches the geographically closest available driver
- **LeastActiveDriverStrategy** — matches the driver with the fewest completed rides

### FR-6: Calculate Fare (pluggable strategies)
- **DefaultFareStrategy** — ₹50 base + ₹10/km
- **PeakHourFareStrategy** — (₹50 + ₹10/km) × 1.5 surge multiplier

### FR-7: Track Ride Status
```
REQUESTED → ASSIGNED → COMPLETED
    ↓           ↓
 CANCELLED   CANCELLED
```
- Invalid state transitions are rejected with `IllegalStateException`

---

## B. Non-Functional Requirements

| Requirement       | Approach                                                 |
|-------------------|----------------------------------------------------------|
| **Extensibility** | Strategy Pattern for matching and fare calculation       |
| **Maintainability** | SOLID principles, high cohesion, low coupling          |
| **Encapsulation** | Unmodifiable collection views, Law of Demeter            |
| **Thread Safety** | `AtomicLong`-based ID generation                         |
| **Testability**   | Constructor injection, no hidden dependencies            |
| **Readability**   | Clean separation into `model/`, `strategy/`, `service/`  |

---

## C. Domain Entities

### Package → `model/`

| Class         | Fields                                                  |
|---------------|---------------------------------------------------------|
| `Rider`       | `id`, `name`, `latitude`, `longitude`, `rideHistory`    |
| `Driver`      | `id`, `name`, `currentLatitude`, `currentLongitude`, `available`, `vehicleType`, `rideHistory` |
| `Ride`        | `id`, `rider`, `driver`, `distance`, `status`, `fareReceipt` |
| `FareReceipt` | `rideId`, `amount`, `generatedAt`                       |

### Enums
| Enum          | Values                                       |
|---------------|----------------------------------------------|
| `RideStatus`  | `REQUESTED`, `ASSIGNED`, `COMPLETED`, `CANCELLED` |
| `VehicleType` | `BIKE`, `AUTO`, `CAR`                        |

---

## D. Strategy & Composition Design

### 1. Ride Matching Strategy
```java
public interface RideMatchingStrategy {
    Driver findDriver(Rider rider, List<Driver> drivers);
}
```
**Implementations:** `NearestDriverStrategy`, `LeastActiveDriverStrategy`

### 2. Fare Calculation Strategy
```java
public interface FareStrategy {
    double calculateFare(Ride ride);
}
```
**Implementations:** `DefaultFareStrategy`, `PeakHourFareStrategy`

Both strategies are **injected into `RideService` via constructor** (Spring DI), ensuring:
- ✅ DIP compliance
- ✅ OCP compliance
- ✅ Composition over inheritance

---

## E. Service Layer

### Package → `service/`

| Service        | Responsibilities                                        |
|----------------|---------------------------------------------------------|
| `RiderService` | Register riders, get rider by ID, list all riders       |
| `DriverService`| Register drivers, update availability, list available drivers |
| `RideService`  | Request ride, assign driver (via strategy), calculate fare (via strategy), complete ride, cancel ride |

---

## F. Console Application (Menu)

Interactive menu in `Main.java`:

```
═══════════════════════════════════════════
         🚕  R I D E W I S E  🚕
═══════════════════════════════════════════
  1. Add Rider
  2. Add Driver
  3. View Available Drivers
  4. Request Ride
  5. Complete Ride
  6. View Rides
  7. Change Matching Strategy
  8. Change Fare Strategy
  9. Exit
═══════════════════════════════════════════
```

Each menu option:
- ✅ Uses **service-layer only** — no direct model mutation
- ✅ **Catches invalid input** gracefully
- ✅ **Avoids tightly-coupled logic** — all orchestration flows through services
