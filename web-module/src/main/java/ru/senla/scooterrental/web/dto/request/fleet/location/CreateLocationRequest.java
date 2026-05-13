package ru.senla.scooterrental.web.dto.request.fleet.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.senla.scooterrental.fleet.enums.LocationType;

public record CreateLocationRequest(

        @NotBlank(message = "Название локации не может быть пустым")
        @Size(max = 255, message = "Название локации слишком длинное")
        String name,

        LocationType type,

        Long parentId

) {
}