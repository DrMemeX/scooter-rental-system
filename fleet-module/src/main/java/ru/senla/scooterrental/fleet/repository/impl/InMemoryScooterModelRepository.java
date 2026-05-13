package ru.senla.scooterrental.fleet.repository.impl;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryScooterModelRepository implements ScooterModelRepository {

    private final AtomicLong idGenerator = new AtomicLong(1);

    private final ConcurrentHashMap<Long, ScooterModel> storage = new ConcurrentHashMap<>();

    @Override
    public ScooterModel save(ScooterModel scooterModel) {
        if (scooterModel == null) {
            throw new FleetValidationException(
                    "Модель самоката не может быть пустой"
            );
        }

        if (scooterModel.getId() != null) {
            validateId(scooterModel.getId());
        }

        if (scooterModel.getId() == null) {
            scooterModel.assignId(idGenerator.getAndIncrement());
        }

        storage.put(scooterModel.getId(), scooterModel);
        return scooterModel;
    }

    @Override
    public Optional<ScooterModel> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ScooterModel> findAll() {
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
    public List<ScooterModel> findAllByScooterClass(ScooterClass scooterClass) {
        if (scooterClass == null) {
            throw new FleetValidationException(
                    "Класс самоката не может быть пустым"
            );
        }

        return storage.values().stream()
                .filter(model -> model.getScooterClass() == scooterClass)
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