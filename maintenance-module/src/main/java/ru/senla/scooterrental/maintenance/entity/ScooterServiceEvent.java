package ru.senla.scooterrental.maintenance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;

import java.time.LocalDateTime;

@Entity
@Table(name = "scooter_service_events")
public class ScooterServiceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceEventType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ScooterServiceEvent() {
    }

    public ScooterServiceEvent(Scooter scooter,
                               ServiceEventType type,
                               String description) {
        this.scooter = requireNonNull(scooter, "Самокат");
        this.type = requireNonNull(type, "Тип сервисного события");
        this.description = normalizeDescription(description);
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Scooter getScooter() {
        return scooter;
    }

    public Long getScooterId() {
        return scooter.getId();
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