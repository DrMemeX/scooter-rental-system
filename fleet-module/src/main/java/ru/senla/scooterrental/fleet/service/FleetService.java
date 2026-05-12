package ru.senla.scooterrental.fleet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class FleetService {

    private final LocationNodeRepository locationNodeRepository;
    private final RentalPointRepository rentalPointRepository;
    private final ScooterRepository scooterRepository;
    private final ScooterModelRepository scooterModelRepository;

    public FleetService(LocationNodeRepository locationNodeRepository,
                        RentalPointRepository rentalPointRepository,
                        ScooterRepository scooterRepository,
                        ScooterModelRepository scooterModelRepository) {
        this.locationNodeRepository = requireNonNull(
                locationNodeRepository, "Репозиторий локаций"
        );
        this.rentalPointRepository = requireNonNull(
                rentalPointRepository, "Репозиторий точек проката"
        );
        this.scooterRepository = requireNonNull(
                scooterRepository, "Репозиторий самокатов"
        );
        this.scooterModelRepository = requireNonNull(
                scooterModelRepository, "Репозиторий моделей самокатов"
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

    public LocationNode renameLocation(Long locationId, String name) {
        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.rename(name);

        return locationNodeRepository.save(locationNode);
    }

    public List<LocationNode> findAllLocations() {
        return locationNodeRepository.findAll();
    }

    public List<LocationNode> findLocationsByType(LocationType type) {
        return locationNodeRepository.findAllByType(type);
    }

    public RentalPoint createRentalPoint(String name, Long locationNodeId) {
        LocationNode locationNode = getLocationOrThrow(locationNodeId);

        RentalPoint rentalPoint = new RentalPoint(name, locationNode);
        return rentalPointRepository.save(rentalPoint);
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

    public RentalPoint renameRentalPoint(Long rentalPointId,
                                         String name) {
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.rename(name);

        return rentalPointRepository.save(rentalPoint);
    }

    public void deleteRentalPoint(Long rentalPointId) {
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        List<Scooter> scooters = scooterRepository.findAllByRentalPointId(rentalPointId);

        if (!scooters.isEmpty()) {
            throw new FleetValidationException(
                    "Нельзя удалить точку проката, пока в ней находятся самокаты"
            );
        }

        rentalPointRepository.deleteById(rentalPoint.getId());
    }

    public List<RentalPoint> findAllRentalPoints() {
        return rentalPointRepository.findAll();
    }

    public List<RentalPoint> findActiveRentalPoints() {
        return rentalPointRepository.findAllActive();
    }

    public RentalPoint getRentalPointById(Long rentalPointId) {
        return getRentalPointOrThrow(rentalPointId);
    }

    public List<Scooter> getRentalPointScooters(Long rentalPointId) {
        getRentalPointOrThrow(rentalPointId);

        return scooterRepository.findAllByRentalPointId(rentalPointId);
    }

    public RentalPoint getActiveRentalPointById(Long rentalPointId) {
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        if (!rentalPoint.isActive()) {
            throw new FleetValidationException(
                    "Точка проката с ID " + rentalPointId + " недоступна"
            );
        }

        return rentalPoint;
    }

    public ScooterModel createScooterModel(ScooterClass scooterClass,
                                           double maxSpeedKmPerHour,
                                           double consumptionPerKm,
                                           BigDecimal pricePerMinute,
                                           BigDecimal pricePerHour,
                                           int batteryCapacity) {
        ScooterModel model = new ScooterModel(
                scooterClass,
                maxSpeedKmPerHour,
                consumptionPerKm,
                pricePerMinute,
                pricePerHour,
                batteryCapacity
        );

        return scooterModelRepository.save(model);
    }

    public ScooterModel getScooterModelById(Long modelId) {
        return getScooterModelOrThrow(modelId);
    }

    public ScooterModel updateScooterModelPrices(Long modelId,
                                                 BigDecimal pricePerMinute,
                                                 BigDecimal pricePerHour) {
        ScooterModel model = getScooterModelOrThrow(modelId);

        model.updatePrices(pricePerMinute, pricePerHour);

        return scooterModelRepository.save(model);
    }

    public List<ScooterModel> findAllScooterModels() {
        return scooterModelRepository.findAll();
    }

    public void deleteScooterModel(Long modelId) {
        ScooterModel model = getScooterModelOrThrow(modelId);

        List<Scooter> scooters = scooterRepository.findAll()
                .stream()
                .filter(scooter -> scooter.getModel().getId().equals(model.getId()))
                .toList();

        if (!scooters.isEmpty()) {
            throw new FleetValidationException(
                    "Нельзя удалить модель самоката, пока существуют самокаты этой модели"
            );
        }

        scooterModelRepository.deleteById(modelId);
    }

    public Scooter createScooter(Long modelId,
                                 Long rentalPointId,
                                 double initialCharge) {
        ScooterModel model = getScooterModelOrThrow(modelId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

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

    public Scooter moveScooterToRentalPoint(Long scooterId,
                                            Long rentalPointId) {

        Scooter scooter = getScooterOrThrow(scooterId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        scooter.moveToRentalPoint(rentalPoint);

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

    public Scooter completeMaintenance(Long scooterId) {
        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.completeMaintenance();

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

    public Scooter getScooterById(Long scooterId) {
        return getScooterOrThrow(scooterId);
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

    public void deleteScooter(Long scooterId) {

        Scooter scooter = getScooterOrThrow(scooterId);

        if (scooter.getStatus() == ScooterStatus.RENTED
                || scooter.getStatus() == ScooterStatus.RETURN_VERIFICATION_REQUIRED) {

            throw new FleetValidationException(
                    "Нельзя удалить самокат, участвующий в активной аренде"
            );
        }

        scooterRepository.deleteById(scooterId);
    }

    private ScooterModel getScooterModelOrThrow(Long modelId) {
        return scooterModelRepository.findById(modelId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Модель самоката с ID " + modelId + " не найдена"
                ));
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