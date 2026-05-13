package ru.senla.scooterrental.web.dto.request.fleet.scooter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoveScooterRequest(

        @NotNull(message = "ID точки проката не может быть пустым")
        @Positive(message = "ID точки проката должен быть положительным")
        Long rentalPointId

) {
}