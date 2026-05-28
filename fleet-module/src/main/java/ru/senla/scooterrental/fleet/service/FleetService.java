package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;

import java.math.BigDecimal;
import java.util.List;

public interface FleetService {

    // Location

    LocationNode createLocation(
            String name,
            LocationType type,
            Long parentId
    );

    LocationNode activateLocation(Long locationId);

    LocationNode deactivateLocation(Long locationId);

    LocationNode renameLocation(
            Long locationId,
            String name
    );

    List<LocationNode> findAllLocations();

    List<LocationNode> findLocationsByType(
            LocationType type
    );


    // RentalPoint

    RentalPoint createRentalPoint(
            String name,
            Long locationNodeId
    );

    RentalPoint activateRentalPoint(
            Long rentalPointId
    );

    RentalPoint deactivateRentalPoint(
            Long rentalPointId
    );

    RentalPoint renameRentalPoint(
            Long rentalPointId,
            String name
    );

    void deleteRentalPoint(
            Long rentalPointId
    );

    RentalPoint getRentalPointById(
            Long rentalPointId
    );

    RentalPoint getActiveRentalPointById(
            Long rentalPointId
    );

    List<RentalPoint> findAllRentalPoints();

    List<RentalPoint> findActiveRentalPoints();

    List<Scooter> getRentalPointScooters(
            Long rentalPointId
    );


    // ScooterModel

    ScooterModel createScooterModel(
            ScooterClass scooterClass,
            double maxSpeedKmPerHour,
            double consumptionPerKm,
            BigDecimal pricePerMinute,
            BigDecimal pricePerHour,
            int batteryCapacity
    );

    ScooterModel getScooterModelById(
            Long modelId
    );

    ScooterModel updateScooterModelPrices(
            Long modelId,
            BigDecimal pricePerMinute,
            BigDecimal pricePerHour
    );

    List<ScooterModel> findAllScooterModels();

    void deleteScooterModel(
            Long modelId
    );


    // Scooter

    Scooter createScooter(
            Long modelId,
            Long rentalPointId,
            double initialCharge
    );

    Scooter getScooterById(
            Long scooterId
    );

    Scooter rentScooter(
            Long scooterId
    );

    Scooter returnScooter(
            Long scooterId,
            Long rentalPointId
    );

    Scooter requestReturnVerification(
            Long scooterId
    );

    Scooter moveScooterToRentalPoint(
            Long scooterId,
            Long rentalPointId
    );

    Scooter sendToMaintenance(
            Long scooterId
    );

    Scooter completeMaintenance(
            Long scooterId
    );

    Scooter markServiceRequired(
            Long scooterId
    );

    Scooter chargeScooter(
            Long scooterId,
            double amount
    );

    Scooter addMileage(
            Long scooterId,
            double km
    );

    Scooter consumeCharge(
            Long scooterId,
            double amount
    );

    List<Scooter> findAllScooters();

    List<Scooter> findAvailableScooters();

    List<Scooter> findScootersByStatus(
            ScooterStatus status
    );

    List<Scooter> findScootersByRentalPoint(
            Long rentalPointId
    );

    void deleteScooter(
            Long scooterId
    );
}