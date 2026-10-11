# RideWise — SOLID Reflection

A principle-by-principle walkthrough showing how each SOLID principle is applied in the codebase.

---

## S — Single Responsibility Principle

> *"A class should have only one reason to change."*

| Class | Single Responsibility | What Would Change It |
|-------|----------------------|----------------------|
| `Rider` | Holds rider state and ride history | Rider data model changes |
| `Driver` | Holds driver state, availability, vehicle type | Driver data model changes |
| `Ride` | Manages ride state transitions and data | Ride life-cycle rule changes |
| `FareReceipt` | Immutable value object for fare data | Receipt format changes |
| `RiderService` | Rider registration and lookup | Rider management rules |
| `DriverService` | Driver registration and availability | Driver management rules |
| `RideService` | Orchestrates the ride life-cycle | Ride workflow changes |
| `NearestDriverStrategy` | One specific matching algorithm | Proximity logic changes |
| `LeastActiveDriverStrategy` | One specific matching algorithm | Workload balancing logic changes |
| `DefaultFareStrategy` | One specific pricing model | Base pricing changes |
| `PeakHourFareStrategy` | One specific pricing model | Surge pricing changes |
| `IdGenerator` | Unique ID generation | ID format changes |

### ✅ Evidence
Changing the fare formula requires modifying **only** the `FareStrategy` implementation.  
`RideService`, `Ride`, `FareReceipt` — all remain untouched.

---

## O — Open/Closed Principle

> *"Open for extension, closed for modification."*

The system is designed so new behavior is added by creating **new classes**, not modifying existing ones.

### Adding a new matching algorithm
```java
// Just create a new class — zero changes to RideService or existing strategies
@Component("highestRatedDriverStrategy")
public class HighestRatedDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        // custom logic
    }
}
```

### Adding a new pricing model
```java
@Component("weekendFareStrategy")
public class WeekendFareStrategy implements FareStrategy {
    @Override
    public double calculateFare(Ride ride) {
        // custom logic
    }
}
```

### ✅ Evidence
Neither `RideService` nor any existing strategy needs modification.  
The new strategy is injected via Spring's DI and selectable at runtime.

---

## L — Liskov Substitution Principle

> *"Subtypes must be substitutable for their base types."*

All strategy implementations are **fully interchangeable**:

### RideMatchingStrategy
- `NearestDriverStrategy.findDriver(rider, drivers)` → returns a `Driver`
- `LeastActiveDriverStrategy.findDriver(rider, drivers)` → returns a `Driver`
- Both accept the same inputs and produce the same output type
- `RideService` works identically regardless of which is injected

### FareStrategy
- `DefaultFareStrategy.calculateFare(ride)` → returns a `double`
- `PeakHourFareStrategy.calculateFare(ride)` → returns a `double`
- Both are fully substitutable — no special handling required by the caller

### ✅ Evidence
The console menu's "Change Matching Strategy" and "Change Fare Strategy" options swap strategies **at runtime**. The rest of the system continues working without any conditional logic.

---

## I — Interface Segregation Principle

> *"No client should be forced to depend on methods it doesn't use."*

| Interface | Methods | Purpose |
|-----------|---------|---------|
| `RideMatchingStrategy` | `findDriver()` | Single method — focused on driver selection |
| `FareStrategy` | `calculateFare()` | Single method — focused on fare computation |

### ✅ Evidence
Each interface has **exactly one method**. No implementor is forced to provide unused functionality.  
Contrast with an anti-pattern:
```java
// ❌ BAD: Fat interface forcing all implementors to handle everything
interface RideHandler {
    Driver findDriver(...);
    double calculateFare(...);
    void sendNotification(...);
}
```

---

## D — Dependency Inversion Principle

> *"Depend on abstractions, not concretions."*

### How it's applied

```
RideService  ──depends on──►  RideMatchingStrategy (interface)
                                       ▲
                                       │ implements
                              ┌────────┴────────┐
                              │                 │
                    NearestDriver       LeastActiveDriver
                    Strategy            Strategy
```

```
RideService  ──depends on──►  FareStrategy (interface)
                                       ▲
                                       │ implements
                              ┌────────┴────────┐
                              │                 │
                    DefaultFare         PeakHourFare
                    Strategy            Strategy
```

### ✅ Evidence — `RideService` constructor:
```java
public RideService(DriverService driverService,
                   RideMatchingStrategy matchingStrategy,    // ← interface
                   FareStrategy fareStrategy) {              // ← interface
    this.driverService = driverService;
    this.matchingStrategy = matchingStrategy;
    this.fareStrategy = fareStrategy;
}
```

`RideService` has **zero imports** of `NearestDriverStrategy`, `LeastActiveDriverStrategy`, `DefaultFareStrategy`, or `PeakHourFareStrategy`. It only knows about the interfaces.

---

## Summary Table

| Principle | Status | Key Evidence |
|-----------|--------|-------------|
| **SRP** | ✅ | Each class has exactly one reason to change |
| **OCP** | ✅ | New strategies = new classes, zero modification |
| **LSP** | ✅ | All strategy implementations are interchangeable at runtime |
| **ISP** | ✅ | Single-method interfaces — no unused dependencies |
| **DIP** | ✅ | `RideService` depends only on `RideMatchingStrategy` and `FareStrategy` interfaces |
