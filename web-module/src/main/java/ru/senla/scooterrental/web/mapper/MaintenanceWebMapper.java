package ru.senla.scooterrental.web.mapper;

import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.web.dto.response.maintenance.MaintenanceEventResponse;

public final class MaintenanceWebMapper {

    private MaintenanceWebMapper() {
    }

    public static MaintenanceEventResponse toResponse(
            ScooterServiceEvent event
    ) {
        return new MaintenanceEventResponse(
                event.getId(),
                event.getScooter().getId(),
                event.getType(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}