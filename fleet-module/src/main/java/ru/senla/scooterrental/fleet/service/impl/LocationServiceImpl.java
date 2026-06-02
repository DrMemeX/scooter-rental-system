package ru.senla.scooterrental.fleet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;
import ru.senla.scooterrental.fleet.service.LocationService;

import java.util.List;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationNodeRepository locationNodeRepository;

    public LocationServiceImpl(LocationNodeRepository locationNodeRepository) {
        this.locationNodeRepository = requireNonNull(locationNodeRepository, "Репозиторий локаций");
    }

    @Override
    public LocationNode createLocation(String name, LocationType type, Long parentId) {
        log.info("Creating location: name={}, type={}, parentId={}", name, type, parentId);

        LocationNode parent = null;

        if (parentId != null) {
            parent = getLocationById(parentId);
        }

        LocationNode locationNode = new LocationNode(name, type, parent);
        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location created successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    @Override
    public LocationNode activateLocation(Long locationId) {
        log.info("Activating location: locationId={}", locationId);

        LocationNode locationNode = getLocationById(locationId);
        locationNode.activate();

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location activated successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    @Override
    public LocationNode deactivateLocation(Long locationId) {
        log.info("Deactivating location: locationId={}", locationId);

        LocationNode locationNode = getLocationById(locationId);
        locationNode.deactivate();

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location deactivated successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    @Override
    public LocationNode renameLocation(Long locationId, String name) {
        log.info("Renaming location: locationId={}, newName={}", locationId, name);

        LocationNode locationNode = getLocationById(locationId);
        locationNode.rename(name);

        LocationNode savedLocation = locationNodeRepository.save(locationNode);

        log.info("Location renamed successfully: locationId={}", savedLocation.getId());

        return savedLocation;
    }

    @Override
    @Transactional(readOnly = true)
    public LocationNode getLocationById(Long locationId) {
        return locationNodeRepository.findById(locationId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Локация с ID " + locationId + " не найдена"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findAllLocations() {
        return locationNodeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findLocationsByType(LocationType type) {
        return locationNodeRepository.findAllByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findChildLocations(Long parentId) {
        getLocationById(parentId);

        return locationNodeRepository.findAllByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findActiveChildLocations(Long parentId) {
        getLocationById(parentId);

        return locationNodeRepository.findAllByParentId(parentId)
                .stream()
                .filter(LocationNode::isActive)
                .toList();
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new FleetValidationException(name + " не задан");
        }

        return obj;
    }
}