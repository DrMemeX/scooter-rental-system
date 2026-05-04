package ru.senla.scooterrental.fleet.repository.impl;

import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRentalPointRepository implements RentalPointRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final ConcurrentHashMap<Long, RentalPoint> storage = new ConcurrentHashMap<>();

    @Override
    public RentalPoint save(RentalPoint rentalPoint) {
        if (rentalPoint == null) {
            throw new FleetValidationException(
                    "Точка проката не может быть пустой"
            );
        }

        if (rentalPoint.getId() != null) {
            validateId(rentalPoint.getId());
        }

        if (rentalPoint.getId() == null) {
            rentalPoint.assignId(idGenerator.getAndIncrement());
        }

        storage.put(rentalPoint.getId(), rentalPoint);
        return rentalPoint;
    }

    @Override
    public Optional<RentalPoint> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RentalPoint> findAll() {
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
    public List<RentalPoint> findAllActive() {
        return storage.values().stream()
                .filter(RentalPoint::isActive)
                .toList();
    }

    @Override
    public List<RentalPoint> findAllByLocationNodeId(Long locationNodeId) {
        validateId(locationNodeId);

        return storage.values().stream()
                .filter(point -> point.getLocationNode() != null)
                .filter(point -> point.getLocationNode().getId() != null)
                .filter(point -> point.getLocationNode().getId().equals(locationNodeId))
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