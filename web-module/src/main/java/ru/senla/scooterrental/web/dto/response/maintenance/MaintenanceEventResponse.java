package ru.senla.scooterrental.web.dto.response.maintenance;

import ru.senla.scooterrental.maintenance.enums.ServiceEventType;

import java.time.LocalDateTime;

public record MaintenanceEventResponse(

        Long id,

        Long scooterId,

        ServiceEventType type,

        String description,

        LocalDateTime createdAt

) {
}