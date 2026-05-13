package ru.senla.scooterrental.fleet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log =
            LoggerFactory.getLogger(FleetService.class);

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
        log.info(
                "Creating location: name={}, type={}, parentId={}",
                name,
                type,
                parentId
        );

        LocationNode parent = null;

        if (parentId != null) {
            parent = getLocationOrThrow(parentId);
        }

        LocationNode locationNode = new LocationNode(name, type, parent);
        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info(
                "Location created successfully: locationId={}, name={}, type={}",
                savedLocation.getId(),
                savedLocation.getName(),
                savedLocation.getType()
        );

        return savedLocation;
    }

    public LocationNode activateLocation(Long locationId) {
        log.info("Activating location: locationId={}", locationId);

        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.activate();

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location activated successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    public LocationNode deactivateLocation(Long locationId) {
        log.info("Deactivating location: locationId={}", locationId);

        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.deactivate();

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location deactivated successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    public LocationNode renameLocation(Long locationId, String name) {
        log.info("Renaming location: locationId={}, newName={}", locationId, name);

        LocationNode locationNode = getLocationOrThrow(locationId);

        locationNode.rename(name);

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info(
                "Location renamed successfully: locationId={}, newName={}",
                savedLocation.getId(),
                savedLocation.getName()
        );

        return savedLocation;
    }

    public List<LocationNode> findAllLocations() {
        return locationNodeRepository.findAll();
    }

    public List<LocationNode> findLocationsByType(LocationType type) {
        return locationNodeRepository.findAllByType(type);
    }

    public RentalPoint createRentalPoint(String name, Long locationNodeId) {
        log.info(
                "Creating rental point: name={}, locationNodeId={}",
                name,
                locationNodeId
        );

        LocationNode locationNode = getLocationOrThrow(locationNodeId);

        RentalPoint rentalPoint = new RentalPoint(name, locationNode);
        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info(
                "Rental point created successfully: rentalPointId={}, name={}, locationNodeId={}",
                savedRentalPoint.getId(),
                savedRentalPoint.getName(),
                locationNodeId
        );

        return savedRentalPoint;
    }

    public RentalPoint activateRentalPoint(Long rentalPointId) {
        log.info("Activating rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.activate();

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info(
                "Rental point activated successfully: rentalPointId={}",
                savedRentalPoint.getId()
        );

        return savedRentalPoint;
    }

    public RentalPoint deactivateRentalPoint(Long rentalPointId) {
        log.info("Deactivating rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.deactivate();

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info(
                "Rental point deactivated successfully: rentalPointId={}",
                savedRentalPoint.getId()
        );

        return savedRentalPoint;
    }

    public RentalPoint renameRentalPoint(Long rentalPointId,
                                         String name) {
        log.info(
                "Renaming rental point: rentalPointId={}, newName={}",
                rentalPointId,
                name
        );

        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        rentalPoint.rename(name);

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info(
                "Rental point renamed successfully: rentalPointId={}, newName={}",
                savedRentalPoint.getId(),
                savedRentalPoint.getName()
        );

        return savedRentalPoint;
    }

    public void deleteRentalPoint(Long rentalPointId) {
        log.info("Deleting rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        List<Scooter> scooters = scooterRepository.findAllByRentalPointId(rentalPointId);

        if (!scooters.isEmpty()) {
            log.warn(
                    "Rental point delete rejected: rentalPointId={}, scootersCount={}",
                    rentalPointId,
                    scooters.size()
            );

            throw new FleetValidationException(
                    "Нельзя удалить точку проката, пока в ней находятся самокаты"
            );
        }

        rentalPointRepository.deleteById(rentalPoint.getId());

        log.info("Rental point deleted successfully: rentalPointId={}", rentalPointId);
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
            log.warn("Inactive rental point requested: rentalPointId={}", rentalPointId);

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
        log.info(
                "Creating scooter model: scooterClass={}, maxSpeed={}, consumptionPerKm={}, pricePerMinute={}, pricePerHour={}, batteryCapacity={}",
                scooterClass,
                maxSpeedKmPerHour,
                consumptionPerKm,
                pricePerMinute,
                pricePerHour,
                batteryCapacity
        );

        ScooterModel model = new ScooterModel(
                scooterClass,
                maxSpeedKmPerHour,
                consumptionPerKm,
                pricePerMinute,
                pricePerHour,
                batteryCapacity
        );

        ScooterModel savedModel = scooterModelRepository.save(model);

        log.info(
                "Scooter model created successfully: modelId={}, scooterClass={}",
                savedModel.getId(),
                savedModel.getScooterClass()
        );

        return savedModel;
    }

    public ScooterModel getScooterModelById(Long modelId) {
        return getScooterModelOrThrow(modelId);
    }

    public ScooterModel updateScooterModelPrices(Long modelId,
                                                 BigDecimal pricePerMinute,
                                                 BigDecimal pricePerHour) {
        log.info(
                "Updating scooter model prices: modelId={}, pricePerMinute={}, pricePerHour={}",
                modelId,
                pricePerMinute,
                pricePerHour
        );

        ScooterModel model = getScooterModelOrThrow(modelId);

        model.updatePrices(pricePerMinute, pricePerHour);

        ScooterModel savedModel = scooterModelRepository.save(model);

        log.info(
                "Scooter model prices updated successfully: modelId={}, pricePerMinute={}, pricePerHour={}",
                savedModel.getId(),
                savedModel.getPricePerMinute(),
                savedModel.getPricePerHour()
        );

        return savedModel;
    }

    public List<ScooterModel> findAllScooterModels() {
        return scooterModelRepository.findAll();
    }

    public void deleteScooterModel(Long modelId) {
        log.info("Deleting scooter model: modelId={}", modelId);

        ScooterModel model = getScooterModelOrThrow(modelId);

        List<Scooter> scooters = scooterRepository.findAll()
                .stream()
                .filter(scooter -> scooter.getModel().getId().equals(model.getId()))
                .toList();

        if (!scooters.isEmpty()) {
            log.warn(
                    "Scooter model delete rejected: modelId={}, scootersCount={}",
                    modelId,
                    scooters.size()
            );

            throw new FleetValidationException(
                    "Нельзя удалить модель самоката, пока существуют самокаты этой модели"
            );
        }

        scooterModelRepository.deleteById(modelId);

        log.info("Scooter model deleted successfully: modelId={}", modelId);
    }

    public Scooter createScooter(Long modelId,
                                 Long rentalPointId,
                                 double initialCharge) {
        log.info(
                "Creating scooter: modelId={}, rentalPointId={}, initialCharge={}",
                modelId,
                rentalPointId,
                initialCharge
        );

        ScooterModel model = getScooterModelOrThrow(modelId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        Scooter scooter = new Scooter(model, rentalPoint, initialCharge);
        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter created successfully: scooterId={}, modelId={}, rentalPointId={}",
                savedScooter.getId(),
                modelId,
                rentalPointId
        );

        return savedScooter;
    }

    public Scooter rentScooter(Long scooterId) {
        log.info("Renting scooter: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.markAsRented();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter rented successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    public Scooter returnScooter(Long scooterId, Long rentalPointId) {
        log.info(
                "Returning scooter: scooterId={}, rentalPointId={}",
                scooterId,
                rentalPointId
        );

        Scooter scooter = getScooterOrThrow(scooterId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        scooter.returnToPoint(rentalPoint);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter returned successfully: scooterId={}, rentalPointId={}",
                savedScooter.getId(),
                rentalPointId
        );

        return savedScooter;
    }

    public Scooter moveScooterToRentalPoint(Long scooterId,
                                            Long rentalPointId) {

        log.info(
                "Moving scooter to rental point: scooterId={}, rentalPointId={}",
                scooterId,
                rentalPointId
        );

        Scooter scooter = getScooterOrThrow(scooterId);
        RentalPoint rentalPoint = getRentalPointOrThrow(rentalPointId);

        scooter.moveToRentalPoint(rentalPoint);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter moved successfully: scooterId={}, rentalPointId={}",
                savedScooter.getId(),
                rentalPointId
        );

        return savedScooter;
    }

    public Scooter requestReturnVerification(Long scooterId) {
        log.info("Requesting scooter return verification: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.requireReturnVerification();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter return verification requested successfully: scooterId={}",
                savedScooter.getId()
        );

        return savedScooter;
    }

    public Scooter sendToMaintenance(Long scooterId) {
        log.info("Sending scooter to maintenance: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.sendToMaintenance();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter sent to maintenance successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    public Scooter completeMaintenance(Long scooterId) {
        log.info("Completing scooter maintenance: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.completeMaintenance();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter maintenance completed successfully: scooterId={}",
                savedScooter.getId()
        );

        return savedScooter;
    }

    public Scooter markServiceRequired(Long scooterId) {
        log.info("Marking scooter service required: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.markServiceRequired();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter marked service required successfully: scooterId={}",
                savedScooter.getId()
        );

        return savedScooter;
    }

    public Scooter chargeScooter(Long scooterId, double amount) {
        log.info(
                "Charging scooter: scooterId={}, amount={}",
                scooterId,
                amount
        );

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.charge(amount);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter charged successfully: scooterId={}, currentCharge={}",
                savedScooter.getId(),
                savedScooter.getCurrentCharge()
        );

        return savedScooter;
    }

    public Scooter addMileage(Long scooterId, double km) {
        log.info(
                "Adding scooter mileage: scooterId={}, km={}",
                scooterId,
                km
        );

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.addMileage(km);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter mileage added successfully: scooterId={}, totalMileage={}",
                savedScooter.getId(),
                savedScooter.getTotalMileageKm()
        );

        return savedScooter;
    }

    public Scooter consumeCharge(Long scooterId, double amount) {
        log.info(
                "Consuming scooter charge: scooterId={}, amount={}",
                scooterId,
                amount
        );

        Scooter scooter = getScooterOrThrow(scooterId);

        scooter.consumeCharge(amount);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info(
                "Scooter charge consumed successfully: scooterId={}, currentCharge={}",
                savedScooter.getId(),
                savedScooter.getCurrentCharge()
        );

        return savedScooter;
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
        log.info("Deleting scooter: scooterId={}", scooterId);

        Scooter scooter = getScooterOrThrow(scooterId);

        if (scooter.getStatus() == ScooterStatus.RENTED
                || scooter.getStatus() == ScooterStatus.RETURN_VERIFICATION_REQUIRED) {

            log.warn(
                    "Scooter delete rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Нельзя удалить самокат, участвующий в активной аренде"
            );
        }

        scooterRepository.deleteById(scooterId);

        log.info("Scooter deleted successfully: scooterId={}", scooterId);
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