package ru.senla.scooterrental.web.dto.request.fleet.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameLocationRequest(

        @NotBlank(message = "Название локации не может быть пустым")
        @Size(max = 255, message = "Название локации слишком длинное")
        String name

) {
}