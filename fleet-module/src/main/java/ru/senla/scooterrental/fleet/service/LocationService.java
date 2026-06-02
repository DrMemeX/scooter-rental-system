package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;

import java.util.List;

public interface LocationService {

    LocationNode createLocation(String name, LocationType type, Long parentId);

    LocationNode activateLocation(Long locationId);

    LocationNode deactivateLocation(Long locationId);

    LocationNode renameLocation(Long locationId, String name);

    LocationNode getLocationById(Long locationId);

    List<LocationNode> findAllLocations();

    List<LocationNode> findLocationsByType(LocationType type);

    List<LocationNode> findChildLocations(Long parentId);

    List<LocationNode> findActiveChildLocations(Long parentId);
}