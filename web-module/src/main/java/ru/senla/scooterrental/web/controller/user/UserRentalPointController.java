package ru.senla.scooterrental.web.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointDetailsResponse;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rental-points")
public class UserRentalPointController {

    private final FleetService fleetService;

    public UserRentalPointController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @GetMapping
    public List<RentalPointResponse> getActiveRentalPoints() {
        return fleetService.findActiveRentalPoints()
                .stream()
                .map(FleetWebMapper::toRentalPointResponse)
                .toList();
    }

    @GetMapping("/{rentalPointId}")
    public RentalPointDetailsResponse getRentalPointDetails(
            @PathVariable Long rentalPointId
    ) {
        RentalPoint rentalPoint = fleetService.getActiveRentalPointById(rentalPointId);

        List<Scooter> scooters = fleetService.getRentalPointScooters(
                rentalPointId
        );

        return FleetWebMapper.toRentalPointDetailsResponse(
                rentalPoint,
                scooters
        );
    }
}