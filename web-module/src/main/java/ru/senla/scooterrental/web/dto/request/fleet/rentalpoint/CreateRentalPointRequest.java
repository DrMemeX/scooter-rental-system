package ru.senla.scooterrental.web.dto.request.fleet.rentalpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRentalPointRequest(

        @NotBlank(message = "Название точки проката не может быть пустым")
        @Size(max = 255, message = "Название точки проката слишком длинное")
        String name,

        @NotNull(message = "ID локации не может быть пустым")
        @Positive(message = "ID локации должен быть положительным")
        Long locationNodeId

) {
}