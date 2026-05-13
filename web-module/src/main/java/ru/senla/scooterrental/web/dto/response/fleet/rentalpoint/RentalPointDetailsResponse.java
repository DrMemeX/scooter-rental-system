package ru.senla.scooterrental.web.dto.response.fleet.rentalpoint;

import ru.senla.scooterrental.web.dto.response.fleet.scooter.ScooterShortResponse;

import java.util.List;

public record RentalPointDetailsResponse(

        RentalPointResponse rentalPoint,

        Integer totalScooters,

        List<ScooterShortResponse> scooters

) {
}
