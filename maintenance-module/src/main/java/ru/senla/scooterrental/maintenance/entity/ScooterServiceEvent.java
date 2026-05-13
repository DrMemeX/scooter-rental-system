package ru.senla.scooterrental.maintenance.entity;

import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;

import java.time.LocalDateTime;

public class ScooterServiceEvent {

    private Long id;

    private final Long scooterId;
    private final ServiceEventType type;
    private final String description;
    private final LocalDateTime createdAt;

    public ScooterServiceEvent(Long scooterId,
                               ServiceEventType type,
                               String description) {
        this.scooterId = requirePositiveId(scooterId, "ID самоката");
        this.type = requireNonNull(type, "Тип сервисного события");
        this.description = normalizeDescription(description);
        this.createdAt = LocalDateTime.now();
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new MaintenanceValidationException(
                    "ID сервисного события уже назначен"
            );
        }

        this.id = requirePositiveId(id, "ID сервисного события");
    }

    public Long getId() {
        return id;
    }

    public Long getScooterId() {
        return scooterId;
    }

    public ServiceEventType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private Long requirePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new MaintenanceValidationException(
                    name + " должен быть положительным"
            );
        }

        return id;
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new MaintenanceValidationException(
                    name + " не задан"
            );
        }

        return obj;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return "Описание не указано";
        }

        return description.trim();
    }
}