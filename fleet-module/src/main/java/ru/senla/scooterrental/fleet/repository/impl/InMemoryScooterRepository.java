package ru.senla.scooterrental.fleet.repository.impl;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryScooterRepository implements ScooterRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final ConcurrentHashMap<Long, Scooter> storage = new ConcurrentHashMap<>();

    @Override
    public Scooter save(Scooter scooter) {
        if (scooter == null) {
            throw new FleetValidationException(
                    "Самокат не может быть пустым"
            );
        }

        if (scooter.getId() != null) {
            validateId(scooter.getId());
        }

        if (scooter.getId() == null) {
            scooter.assignId(idGenerator.getAndIncrement());
        }

        storage.put(scooter.getId(), scooter);
        return scooter;
    }

    @Override
    public Optional<Scooter> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Scooter> findAll() {
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
    public List<Scooter> findAllByStatus(ScooterStatus status) {
        if (status == null) {
            throw new FleetValidationException(
                    "Статус самоката не может быть пустым"
            );
        }

        return storage.values().stream()
                .filter(scooter -> scooter.getStatus() == status)
                .toList();
    }

    @Override
    public List<Scooter> findAllAvailable() {
        return storage.values().stream()
                .filter(Scooter::isAvailable)
                .toList();
    }

    @Override
    public List<Scooter> findAllByRentalPointId(Long rentalPointId) {
        validateId(rentalPointId);

        return storage.values().stream()
                .filter(scooter -> scooter.getCurrentRentalPoint() != null)
                .filter(scooter -> scooter.getCurrentRentalPoint().getId() != null)
                .filter(scooter -> scooter.getCurrentRentalPoint().getId().equals(rentalPointId))
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