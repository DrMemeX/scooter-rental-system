package ru.senla.scooterrental.fleet.repository.impl;

import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryLocationNodeRepository implements LocationNodeRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final ConcurrentHashMap<Long, LocationNode> storage = new ConcurrentHashMap<>();

    @Override
    public LocationNode save(LocationNode locationNode) {
        if (locationNode == null) {
            throw new FleetValidationException(
                    "Локация не может быть пустой"
            );
        }

        if (locationNode.getId() != null) {
            validateId(locationNode.getId());
        }

        if (locationNode.getId() == null) {
            locationNode.assignId(idGenerator.getAndIncrement());
        }

        storage.put(locationNode.getId(), locationNode);
        return locationNode;
    }

    @Override
    public Optional<LocationNode> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<LocationNode> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id);
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);
        storage.remove(id);
    }

    @Override
    public List<LocationNode> findAllByType(LocationType type) {
        if (type == null) {
            throw new FleetValidationException(
                    "Тип локации не может быть пустым"
            );
        }

        return storage.values().stream()
                .filter(location -> location.getType() == type)
                .toList();
    }

    @Override
    public List<LocationNode> findAllActive() {
        return storage.values().stream()
                .filter(LocationNode::isActive)
                .toList();
    }

    @Override
    public List<LocationNode> findAllByParentId(Long parentId) {
        validateId(parentId);

        return storage.values().stream()
                .filter(location -> location.getParent() != null)
                .filter(location -> location.getParent().getId() != null)
                .filter(location -> location.getParent().getId().equals(parentId))
                .toList();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new FleetValidationException(
                    "ID должен быть положительным числом"
            );
        }
    }
}