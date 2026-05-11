package ru.senla.scooterrental.web.dto.request.fleet.rentalpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameRentalPointRequest(

        @NotBlank(message = "Название точки проката не может быть пустым")
        @Size(max = 255, message = "Название точки проката слишком длинное")
        String name

) {
}