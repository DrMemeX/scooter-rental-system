package ru.senla.scooterrental.web.dto.response.fleet.rentalpoint;

import ru.senla.scooterrental.fleet.enums.LocationType;

public record RentalPointResponse(

        Long id,

        String name,

        Boolean active,

        Long locationNodeId,

        String locationName,

        LocationType locationType

) {
}