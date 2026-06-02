package ru.senla.scooterrental.web.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.RentalPointService;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointDetailsResponse;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rental-points")
public class UserRentalPointController {

    private final RentalPointService rentalPointService;

    public UserRentalPointController(RentalPointService rentalPointService) {
        this.rentalPointService = rentalPointService;
    }

    @GetMapping
    public List<RentalPointResponse> getActiveRentalPoints() {
        return rentalPointService.findActiveRentalPoints()
                .stream()
                .map(FleetWebMapper::toRentalPointResponse)
                .toList();
    }

    @GetMapping("/by-location/{locationNodeId}")
    public List<RentalPointResponse> getActiveRentalPointsByLocation(
            @PathVariable("locationNodeId") Long locationNodeId
    ) {
        return rentalPointService.findActiveRentalPointsByLocationNode(locationNodeId)
                .stream()
                .map(FleetWebMapper::toRentalPointResponse)
                .toList();
    }

    @GetMapping("/{rentalPointId}")
    public RentalPointDetailsResponse getRentalPointDetails(
            @PathVariable("rentalPointId") Long rentalPointId
    ) {
        RentalPoint rentalPoint = rentalPointService.getActiveRentalPointById(rentalPointId);

        List<Scooter> scooters = rentalPointService.getRentalPointScooters(
                rentalPointId
        );

        return FleetWebMapper.toRentalPointDetailsResponse(
                rentalPoint,
                scooters
        );
    }
}