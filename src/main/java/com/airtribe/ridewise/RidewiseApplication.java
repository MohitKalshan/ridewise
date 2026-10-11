package com.airtribe.ridewise;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@SpringBootApplication
public class RidewiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RidewiseApplication.class, args);
    }

    @Bean
    CommandLineRunner run(RiderService riderService,
                          DriverService driverService,
                          RideService rideService,
                          NearestDriverStrategy nearestStrategy,
                          LeastActiveDriverStrategy leastActiveStrategy,
                          DefaultFareStrategy defaultFareStrategy,
                          PeakHourFareStrategy peakHourFareStrategy) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                printMenu();
                if (!scanner.hasNextLine()) {
                    break;
                }
                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> addRider(scanner, riderService);
                        case "2" -> addDriver(scanner, driverService);
                        case "3" -> viewAvailableDrivers(driverService);
                        case "4" -> requestRide(scanner, riderService, rideService);
                        case "5" -> completeRide(scanner, rideService);
                        case "6" -> viewRides(rideService);
                        case "7" -> changeMatchingStrategy(scanner, rideService, nearestStrategy, leastActiveStrategy);
                        case "8" -> changeFareStrategy(scanner, rideService, defaultFareStrategy, peakHourFareStrategy);
                        case "9" -> {
                            System.out.println("\n👋 Goodbye! Thanks for using RideWise.");
                            running = false;
                            System.exit(0);
                        }
                        default -> System.out.println("⚠️  Invalid option. Please enter 1-9.");
                    }
                } catch (NoDriverAvailableException e) {
                    System.out.println("❌ " + e.getMessage());
                } catch (IllegalStateException e) {
                    System.out.println("⚠️  " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("⚠️  Invalid input: " + e.getMessage());
                }
            }

            scanner.close();
        };
    }

    // ── Menu ─────────────────────────────────────────────────

    private void printMenu() {
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("         🚕  R I D E W I S E  🚕         ");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  1. Add Rider");
        System.out.println("  2. Add Driver");
        System.out.println("  3. View Available Drivers");
        System.out.println("  4. Request Ride");
        System.out.println("  5. Complete Ride");
        System.out.println("  6. View Rides");
        System.out.println("  7. Change Matching Strategy");
        System.out.println("  8. Change Fare Strategy");
        System.out.println("  9. Exit");
        System.out.println("═══════════════════════════════════════════");
        System.out.print("Choose an option: ");
    }

    // ── 1. Add Rider ─────────────────────────────────────────

    private void addRider(Scanner scanner, RiderService riderService) {
        System.out.print("Enter rider name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter latitude: ");
        double lat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter longitude: ");
        double lon = Double.parseDouble(scanner.nextLine().trim());

        Rider rider = riderService.registerRider(name, lat, lon);
        System.out.println("✅ Registered: " + rider);
    }

    // ── 2. Add Driver ────────────────────────────────────────

    private void addDriver(Scanner scanner, DriverService driverService) {
        System.out.print("Enter driver name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter latitude: ");
        double lat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter longitude: ");
        double lon = Double.parseDouble(scanner.nextLine().trim());

        System.out.println("Select vehicle type:");
        System.out.println("  1. BIKE");
        System.out.println("  2. AUTO");
        System.out.println("  3. CAR");
        System.out.print("Choice: ");
        String vehicleChoice = scanner.nextLine().trim();

        VehicleType vehicleType = switch (vehicleChoice) {
            case "1" -> VehicleType.BIKE;
            case "2" -> VehicleType.AUTO;
            case "3" -> VehicleType.CAR;
            default -> {
                System.out.println("⚠️  Invalid choice, defaulting to CAR.");
                yield VehicleType.CAR;
            }
        };

        Driver driver = driverService.registerDriver(name, lat, lon, vehicleType);
        System.out.println("✅ Registered: " + driver);
    }

    // ── 3. View Available Drivers ────────────────────────────

    private void viewAvailableDrivers(DriverService driverService) {
        List<Driver> available = driverService.getAvailableDrivers();
        if (available.isEmpty()) {
            System.out.println("📭 No drivers currently available.");
            return;
        }
        System.out.println("\n🟢 Available Drivers:");
        available.forEach(d -> System.out.println("  " + d));
    }

    // ── 4. Request Ride ──────────────────────────────────────

    private void requestRide(Scanner scanner, RiderService riderService, RideService rideService) {
        // Show registered riders
        List<Rider> riders = riderService.getAllRiders();
        if (riders.isEmpty()) {
            System.out.println("⚠️  No riders registered. Add a rider first.");
            return;
        }
        System.out.println("\nRegistered Riders:");
        riders.forEach(r -> System.out.println("  " + r.getId() + " — " + r.getName()));

        System.out.print("Enter Rider ID: ");
        String riderId = scanner.nextLine().trim();

        Optional<Rider> riderOpt = riderService.findById(riderId);
        if (riderOpt.isEmpty()) {
            System.out.println("⚠️  Rider not found: " + riderId);
            return;
        }

        System.out.print("Enter pickup latitude: ");
        double pickupLat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter pickup longitude: ");
        double pickupLon = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter drop-off latitude: ");
        double dropLat = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Enter drop-off longitude: ");
        double dropLon = Double.parseDouble(scanner.nextLine().trim());

        Ride ride = rideService.requestRide(riderOpt.get(), pickupLat, pickupLon, dropLat, dropLon);
        System.out.println("\n✅ Ride booked!");
        System.out.println("   " + ride);
    }

    // ── 5. Complete Ride ─────────────────────────────────────

    private void completeRide(Scanner scanner, RideService rideService) {
        List<Ride> rides = rideService.getAllRides();
        if (rides.isEmpty()) {
            System.out.println("⚠️  No rides in the system.");
            return;
        }

        System.out.println("\nAll Rides:");
        rides.forEach(r -> System.out.println("  " + r));

        System.out.print("Enter Ride ID to complete: ");
        String rideId = scanner.nextLine().trim();

        Optional<Ride> rideOpt = rideService.findById(rideId);
        if (rideOpt.isEmpty()) {
            System.out.println("⚠️  Ride not found: " + rideId);
            return;
        }

        FareReceipt receipt = rideService.completeRide(rideOpt.get());
        System.out.println("✅ Ride completed!");
        System.out.println("   💰 " + receipt);
    }

    // ── 6. View Rides ────────────────────────────────────────

    private void viewRides(RideService rideService) {
        List<Ride> rides = rideService.getAllRides();
        if (rides.isEmpty()) {
            System.out.println("📭 No rides yet.");
            return;
        }
        System.out.println("\n📋 All Rides:");
        rides.forEach(r -> System.out.println("  " + r));
    }

    // ── 7. Change Matching Strategy ──────────────────────────

    private void changeMatchingStrategy(Scanner scanner, RideService rideService,
                                        NearestDriverStrategy nearestStrategy,
                                        LeastActiveDriverStrategy leastActiveStrategy) {
        System.out.println("\nCurrent strategy: " + rideService.getMatchingStrategyName());
        System.out.println("  1. NearestDriverStrategy");
        System.out.println("  2. LeastActiveDriverStrategy");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                rideService.setMatchingStrategy(nearestStrategy);
                System.out.println("🔄 Switched to NearestDriverStrategy");
            }
            case "2" -> {
                rideService.setMatchingStrategy(leastActiveStrategy);
                System.out.println("🔄 Switched to LeastActiveDriverStrategy");
            }
            default -> System.out.println("⚠️  Invalid choice.");
        }
    }

    // ── 8. Change Fare Strategy ──────────────────────────────

    private void changeFareStrategy(Scanner scanner, RideService rideService,
                                    DefaultFareStrategy defaultFareStrategy,
                                    PeakHourFareStrategy peakHourFareStrategy) {
        System.out.println("\nCurrent strategy: " + rideService.getFareStrategyName());
        System.out.println("  1. DefaultFareStrategy");
        System.out.println("  2. PeakHourFareStrategy");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                rideService.setFareStrategy(defaultFareStrategy);
                System.out.println("🔄 Switched to DefaultFareStrategy");
            }
            case "2" -> {
                rideService.setFareStrategy(peakHourFareStrategy);
                System.out.println("🔄 Switched to PeakHourFareStrategy");
            }
            default -> System.out.println("⚠️  Invalid choice.");
        }
    }
}
