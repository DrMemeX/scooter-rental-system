package ru.senla.scooterrental.maintenance.service;

import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;

import java.util.List;

public interface MaintenanceService {

    ScooterServiceEvent reportTechnicalBreakdown(
            Long scooterId,
            String description
    );

    ScooterServiceEvent reportUserDamage(
            Long scooterId,
            String description
    );

    ScooterServiceEvent sendToMaintenance(
            Long scooterId,
            String description
    );

    ScooterServiceEvent completeMaintenance(
            Long scooterId,
            String description
    );

    ScooterServiceEvent chargeScooter(
            Long scooterId,
            double amount,
            String description
    );

    ScooterServiceEvent markServiceRequired(
            Long scooterId,
            String description
    );

    List<ScooterServiceEvent> getAllEvents();

    List<ScooterServiceEvent> getEventsByScooterId(
            Long scooterId
    );

    List<ScooterServiceEvent> getEventsByType(
            ServiceEventType type
    );
}