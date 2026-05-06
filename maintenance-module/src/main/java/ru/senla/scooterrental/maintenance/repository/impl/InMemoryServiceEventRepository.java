package ru.senla.scooterrental.maintenance.repository.impl;

import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.exceptions.ServiceEventNotFoundException;
import ru.senla.scooterrental.maintenance.repository.ServiceEventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryServiceEventRepository implements ServiceEventRepository {

    private final List<ScooterServiceEvent> events = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public ScooterServiceEvent save(ScooterServiceEvent event) {
        if (event == null) {
            throw new MaintenanceValidationException(
                    "Сервисное событие не может быть пустым"
            );
        }

        if (event.getId() == null) {
            event.assignId(nextId++);
            events.add(event);
            return event;
        }

        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(event.getId())) {
                events.set(i, event);
                return event;
            }
        }

        throw new ServiceEventNotFoundException(
                "Невозможно обновить сервисное событие: объект с ID "
                        + event.getId() + " не найден"
        );
    }

    @Override
    public Optional<ScooterServiceEvent> findById(Long id) {
        validateId(id, "ID сервисного события");

        return events.stream()
                .filter(event -> event.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<ScooterServiceEvent> findAll() {
        return new ArrayList<>(events);
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id, "ID сервисного события");

        return events.stream()
                .anyMatch(event -> event.getId().equals(id));
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID сервисного события");

        events.removeIf(event -> event.getId().equals(id));
    }

    @Override
    public List<ScooterServiceEvent> findAllByScooterId(Long scooterId) {
        validateId(scooterId, "ID самоката");

        return events.stream()
                .filter(event -> event.getScooterId().equals(scooterId))
                .toList();
    }

    @Override
    public List<ScooterServiceEvent> findAllByType(ServiceEventType type) {
        if (type == null) {
            throw new MaintenanceValidationException(
                    "Тип сервисного события не может быть пустым"
            );
        }

        return events.stream()
                .filter(event -> event.getType() == type)
                .toList();
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new MaintenanceValidationException(
                    name + " должен быть положительным"
            );
        }
    }
}