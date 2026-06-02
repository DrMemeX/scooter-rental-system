package ru.senla.scooterrental.fleet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.LocationService;
import ru.senla.scooterrental.fleet.service.RentalPointService;

import java.util.List;

@Service
@Transactional
public class RentalPointServiceImpl implements RentalPointService {

    private static final Logger log = LoggerFactory.getLogger(RentalPointServiceImpl.class);

    private final RentalPointRepository rentalPointRepository;
    private final ScooterRepository scooterRepository;
    private final LocationService locationService;

    public RentalPointServiceImpl(RentalPointRepository rentalPointRepository,
                                  ScooterRepository scooterRepository,
                                  LocationService locationService) {
        this.rentalPointRepository = requireNonNull(rentalPointRepository, "Репозиторий точек проката");
        this.scooterRepository = requireNonNull(scooterRepository, "Репозиторий самокатов");
        this.locationService = requireNonNull(locationService, "Сервис локаций");
    }

    @Override
    public RentalPoint createRentalPoint(String name, Long locationNodeId) {
        log.info("Creating rental point: name={}, locationNodeId={}", name, locationNodeId);

        LocationNode locationNode = locationService.getLocationById(locationNodeId);

        RentalPoint rentalPoint = new RentalPoint(name, locationNode);
        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info("Rental point created successfully: rentalPointId={}", savedRentalPoint.getId());

        return savedRentalPoint;
    }

    @Override
    public RentalPoint activateRentalPoint(Long rentalPointId) {
        log.info("Activating rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointById(rentalPointId);
        rentalPoint.activate();

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info("Rental point activated successfully: rentalPointId={}", savedRentalPoint.getId());

        return savedRentalPoint;
    }

    @Override
    public RentalPoint deactivateRentalPoint(Long rentalPointId) {
        log.info("Deactivating rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointById(rentalPointId);
        rentalPoint.deactivate();

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info("Rental point deactivated successfully: rentalPointId={}", savedRentalPoint.getId());

        return savedRentalPoint;
    }

    @Override
    public RentalPoint renameRentalPoint(Long rentalPointId, String name) {
        log.info("Renaming rental point: rentalPointId={}, newName={}", rentalPointId, name);

        RentalPoint rentalPoint = getRentalPointById(rentalPointId);
        rentalPoint.rename(name);

        RentalPoint savedRentalPoint = rentalPointRepository.save(rentalPoint);

        log.info("Rental point renamed successfully: rentalPointId={}", savedRentalPoint.getId());

        return savedRentalPoint;
    }

    @Override
    public void deleteRentalPoint(Long rentalPointId) {
        log.info("Deleting rental point: rentalPointId={}", rentalPointId);

        RentalPoint rentalPoint = getRentalPointById(rentalPointId);
        List<Scooter> scooters = scooterRepository.findAllByRentalPointId(rentalPointId);

        if (!scooters.isEmpty()) {
            log.warn("Rental point delete rejected: rentalPointId={}, scootersCount={}",
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

    @Override
    @Transactional(readOnly = true)
    public RentalPoint getRentalPointById(Long rentalPointId) {
        return rentalPointRepository.findById(rentalPointId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Точка проката с ID " + rentalPointId + " не найдена"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public RentalPoint getActiveRentalPointById(Long rentalPointId) {
        RentalPoint rentalPoint = getRentalPointById(rentalPointId);

        if (!rentalPoint.isActive()) {
            log.warn("Inactive rental point requested: rentalPointId={}", rentalPointId);

            throw new FleetValidationException(
                    "Точка проката с ID " + rentalPointId + " недоступна"
            );
        }

        return rentalPoint;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findAllRentalPoints() {
        return rentalPointRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findActiveRentalPoints() {
        return rentalPointRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findRentalPointsByLocationNode(Long locationNodeId) {
        locationService.getLocationById(locationNodeId);

        return rentalPointRepository.findAllByLocationNodeId(locationNodeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findActiveRentalPointsByLocationNode(Long locationNodeId) {
        locationService.getLocationById(locationNodeId);

        return rentalPointRepository.findAllByLocationNodeId(locationNodeId)
                .stream()
                .filter(RentalPoint::isActive)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scooter> getRentalPointScooters(Long rentalPointId) {
        getRentalPointById(rentalPointId);

        return scooterRepository.findAllByRentalPointId(rentalPointId);
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new FleetValidationException(name + " не задан");
        }

        return obj;
    }
}