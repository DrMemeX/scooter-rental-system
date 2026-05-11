package ru.senla.scooterrental.web.dto.request.fleet.scooter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateScooterRequest(

        @NotNull(message = "ID модели самоката не может быть пустым")
        @Positive(message = "ID модели самоката должен быть положительным")
        Long modelId,

        @NotNull(message = "ID точки проката не может быть пустым")
        @Positive(message = "ID точки проката должен быть положительным")
        Long rentalPointId,

        @Positive(message = "Начальный заряд должен быть положительным")
        double initialCharge

) {
}