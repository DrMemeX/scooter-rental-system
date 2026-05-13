package ru.senla.scooterrental.web.dto.response.fleet.location;

import ru.senla.scooterrental.fleet.enums.LocationType;

public record LocationResponse(

        Long id,

        String name,

        LocationType type,

        Long parentId,

        Boolean active

) {
}
