package ru.senla.scooterrental.web.dto.request.maintenance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ChargeScooterRequest(
        @NotNull(message = "Объем зарядки должен быть указан")
        @Positive(message = "Объем зарядки должен быть положительным")
        Double amount,

        @Size(max = 500, message = "Описание события слишком длинное")
        String description
) {
}
