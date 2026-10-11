# RideWise — Modular Ride-Sharing System

A console-based ride-sharing application (Uber/Ola style) built with **Spring Boot**.  
The goal is to demonstrate solid **Low-Level Design (LLD)** and **OOP/SOLID** design thinking — clean entities, layered services, and pluggable strategies — rather than a production-ready product.

---

## Features

- Register riders with a name and GPS location
- Register drivers with a name, GPS location, and vehicle type (`BIKE`, `AUTO`, `CAR`)
- List available drivers
- Request a ride and auto-match a driver via a pluggable matching strategy
- Calculate the fare via a pluggable pricing strategy and issue a `FareReceipt`
- Track the ride life-cycle with a validated state machine
- Swap matching and fare strategies **at runtime** from the menu

### Ride status flow

```
REQUESTED ──assign──▶ ASSIGNED ──complete──▶ COMPLETED
     │                    │
     └──── cancel ────────┴──────▶ CANCELLED
```

Invalid transitions (e.g. completing a `REQUESTED` ride) are rejected with `IllegalStateException`.

---

## Tech Stack

| Tool | Version |
|------|---------|
| Java | 22 (toolchain) |
| Spring Boot | 4.1.1 |
| Build | Gradle (wrapper 9.7.1) |
| Testing | JUnit 5 via `spring-boot-starter-test` |

---

## Architecture

Layered packages keep each concern separate and dependency directions clean.

```
com.airtribe.ridewise
├── RidewiseApplication        # Entry point + interactive console menu (CommandLineRunner)
├── model/                     # Domain entities & enums
│   ├── Rider
│   ├── Driver
│   ├── Ride                   # Owns its state transitions + composed FareReceipt
│   ├── FareReceipt            # Immutable value object
│   ├── RideStatus             # enum
│   └── VehicleType            # enum
├── service/                   # Orchestration layer
│   ├── RiderService
│   ├── DriverService
│   └── RideService            # Injects the two strategies (DIP)
├── strategy/                  # Extension points
│   ├── RideMatchingStrategy   # interface
│   │   ├── NearestDriverStrategy
│   │   └── LeastActiveDriverStrategy
│   └── FareStrategy           # interface
│       ├── DefaultFareStrategy
│       └── PeakHourFareStrategy
├── exception/
│   └── NoDriverAvailableException
└── util/
    └── IdGenerator            # AtomicLong-based, thread-safe IDs
```

### Domain relationships

| From → To | Type | Notes |
|-----------|------|-------|
| `Ride` → `Rider` / `Driver` | Association | A ride references but does not own them |
| `Ride` → `FareReceipt` | **Composition** | A receipt has no meaning outside its ride |
| `Rider` → `Ride` (`rideHistory`) | Association | Exposed as unmodifiable views |
| `Driver` → `Ride` (`rideHistory`) | Association | Backs `getCompletedRideCount()` |
| `RideService` → `RideMatchingStrategy` / `FareStrategy` | **Composition** | Swappable strategy objects |

---

## Design Principles

### SOLID

| Principle | How it is applied |
|-----------|-------------------|
| **SRP** | Each class has one reason to change: entities hold state, services orchestrate, strategies encapsulate a single algorithm |
| **OCP** | New matching/pricing behavior is added as a new class — no changes to `RideService` or existing strategies |
| **LSP** | Every `RideMatchingStrategy`/`FareStrategy` implementation is fully interchangeable at runtime |
| **ISP** | Single-method interfaces: `findDriver()` and `calculateFare()` |
| **DIP** | `RideService` depends only on the strategy interfaces, injected via its constructor |

### Other principles

- **DRY** — ride allocation lives in one place (`RideService`); no duplicated logic.
- **KISS** — minimal, focused entity fields.
- **YAGNI** — MVP feature set only.
- **Law of Demeter** — collaborators expose derived state (e.g. `driver.getCompletedRideCount()`) instead of leaking internal collections.

### Composition over inheritance

Instead of an `N × M` subclass explosion (matching × fare combinations), the two strategy dimensions are composed into `RideService` and mixed freely: **2 matching + 2 fare = 4 classes**, not 4 subclasses.

---

## Getting Started

### Prerequisites

- JDK 22 or newer
- No local Gradle needed — the Gradle wrapper is included

### Build

```bash
./gradlew clean build
```

### Run

```bash
./gradlew bootRun
```

The app is a console program. (`bootRun` forwards stdin so the menu is interactive.)

You can also run the packaged jar:

```bash
./gradlew bootJar
java -jar build/libs/ridewise-0.0.1-SNAPSHOT.jar
```

### Test

```bash
./gradlew test
```

---

## Usage

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

Every option goes **through the service layer only** — no direct model mutation from the UI — and invalid input is caught and reported without crashing.

### Strategies available at runtime

| Menu | Options |
|------|---------|
| Matching (7) | `NearestDriverStrategy`, `LeastActiveDriverStrategy` |
| Fare (8) | `DefaultFareStrategy` (₹50 + ₹10/km), `PeakHourFareStrategy` ((₹50 + ₹10/km) × 1.5) |

### Sample session

```
1                      → Add Rider   Bob   (12.97, 77.59)
2                      → Add Driver  Alice (12.98, 77.60) CAR
4  RIDER-1 ...         → Request Ride → matched to DRIVER-1 (ASSIGNED)
5  RIDE-1              → Complete Ride → FareReceipt ₹81.05
6                      → View Rides (status COMPLETED, fare recorded)
```

If no driver is available, the request fails cleanly with `NoDriverAvailableException` and **no ride is created**.

---

## Extending the System (OCP in action)

Adding a new algorithm requires **only a new class** — nothing else changes:

```java
@Component
public class HighestRatedDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        // custom logic
    }
}
```

To make it selectable from the console, wire it into the `Change Matching Strategy` menu in `RidewiseApplication`.

---

## Documentation

Design deep-dives live alongside the code in `src/main/java/com/airtribe/ridewise/docs/`:

- `Requirements.md` — functional & non-functional requirements
- `Class_Model.md` — class diagram and entity summary
- `Object_Relationships.md` — relationship types and rationale
- `SOLID_Reflection.md` — principle-by-principle walkthrough

---

## Project Structure

```
.
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── src
    ├── main
    │   ├── java/com/airtribe/ridewise/...   # application, model, service, strategy, util, docs
    │   └── resources/application.properties
    └── test/java/com/airtribe/ridewise/     # Spring context test
```
