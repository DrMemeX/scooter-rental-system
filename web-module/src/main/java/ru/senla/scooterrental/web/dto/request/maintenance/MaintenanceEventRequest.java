package ru.senla.scooterrental.web.dto.request.maintenance;

import jakarta.validation.constraints.Size;

public record MaintenanceEventRequest(
        @Size(max = 500, message = "Описание события слишком длинное")
        String description
) {
}