package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.factory.ScooterModelFactory;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.valueobject.ScooterModel;

import java.util.List;

public class FleetService {

    private final LocationNodeRepository locationNodeRepository;
    private final RentalPointRepository rentalPointRepository;
    private final ScooterRepository scooterRepository;

    public FleetService(LocationNodeRepository locationNodeRepository,
                        RentalPointRepository rentalPointRepository,
                        ScooterRepository scooterRepository) {
        this.locationNodeRepository = requireNonNull(
                locationNodeRepository, "Репозиторий локаций"
        );
        this.rentalPointRepository = requireNonNull(
                rentalPointRepository, "Репозиторий точек проката"
        );
        this.scooterRepository = requireNonNull(
                scooterRepository, "Репозиторий самокатов"
        );
    }

    public LocationNode createLocation(String name,
                                       LocationType type,
                                       Long parentId) {
        LocationNode parent = null;

        if (parentId != null) {
            parent = getLocationOrThrow(parentId);
        }

        LocationNode locationNode = new LocationNode(name, type, parent);
        return locationNodeRepository.save(locationNode);
    }

    public RentalPoint createRentalPoint(String name, Long locationNodeId) {
        LocationNode locationNode = getLocationOrThrow(locationNodeId);

        RentalPoint rentalPoint = new RentalPoint(name, locationNode);
        return rentalPointRepository.save(rentalPoint);
    }

    public Scooter createScooter(ScooterClass scooterClass,
                                 Long rentalPointId,
                                 double initialCharge) {
        if (scooterClass == null) {
            throw new FleetValidationException(
                    "Класс самоката не может быть пустым"
            );
        }

        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);
        ScooterModel model = ScooterModelFactory.create(scooterClass);

        Scooter scooter = new Scooter(model, rentalPoint, initialCharge);
        return scooterRepository.save(scooter);
    }

    public Scooter rentScooter(Long scooterId) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.markAsRented();

        return scooterRepository.save(scooter);
    }

    public Scooter returnScooter(Long scooterId, Long rentalPointId) {

        Scooter scooter = getScooterOrThrow(scooterId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        scooter.returnToPoint(rentalPoint);

        return scooterRepository.save(scooter);
    }

    public Scooter requestReturnVerification(Long scooterId) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.requireReturnVerification();

        return scooterRepository.save(scooter);
    }

    public Scooter sendToMaintenance(Long scooterId) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.sendToMaintenance();

        return scooterRepository.save(scooter);
    }

    public Scooter markServiceRequired(Long scooterId) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.markServiceRequired();

        return scooterRepository.save(scooter);
    }

    public Scooter chargeScooter(Long scooterId, double amount) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.charge(amount);

        return scooterRepository.save(scooter);
    }

    public Scooter addMileage(Long scooterId, double km) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.addMileage(km);

        return scooterRepository.save(scooter);
    }

    public Scooter consumeCharge(Long scooterId, double amount) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.consumeCharge(amount);

        return scooterRepository.save(scooter);
    }

    public LocationNode activateLocation(Long locationId) {
        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.activate();

        return locationNodeRepository.save(locationNode);
    }

    public LocationNode deactivateLocation(Long locationId) {
        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.deactivate();

        return locationNodeRepository.save(locationNode);
    }

    public RentalPoint activateRentalPoint(Long rentalPointId) {
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.activate();

        return rentalPointRepository.save(rentalPoint);
    }

    public RentalPoint deactivateRentalPoint(Long rentalPointId) {
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.deactivate();

        return rentalPointRepository.save(rentalPoint);
    }

    public List<Scooter> findAllScooters() {
        return scooterRepository.findAll();
    }

    public List<Scooter> findAvailableScooters() {
        return scooterRepository.findAllAvailable();
    }

    public List<Scooter> findScootersByStatus(ScooterStatus status) {
        return scooterRepository.findAllByStatus(status);
    }

    public List<Scooter> findScootersByRentalPoint(Long rentalPointId) {
        return scooterRepository.findAllByRentalPointId(rentalPointId);
    }

    public List<RentalPoint> findAllRentalPoints() {
        return rentalPointRepository.findAll();
    }

    public List<RentalPoint> findActiveRentalPoints() {
        return rentalPointRepository.findAllActive();
    }

    public List<LocationNode> findAllLocations() {
        return locationNodeRepository.findAll();
    }

    public List<LocationNode> findLocationsByType(LocationType type) {
        return locationNodeRepository.findAllByType(type);
    }

    public Scooter getScooterById(Long scooterId) {
        return getScooterOrThrow(scooterId);
    }

    private Scooter getScooterOrThrow(Long scooterId) {
        return scooterRepository.findById(scooterId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Самокат с ID " + scooterId + " не найден"
                ));
    }

    private RentalPoint getRentalPointOrThrow(Long rentalPointId) {
        return rentalPointRepository.findById(rentalPointId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Точка проката с ID " + rentalPointId + " не найдена"
                ));
    }

    private LocationNode getLocationOrThrow(Long locationId) {
        return locationNodeRepository.findById(locationId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Локация с ID " + locationId + " не найдена"
                ));
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new FleetValidationException(
                    name + " не задан"
            );
        }
        return obj;
    }
}