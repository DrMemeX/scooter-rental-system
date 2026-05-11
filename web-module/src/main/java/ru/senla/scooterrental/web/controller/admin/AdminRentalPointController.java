package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.CreateRentalPointRequest;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.RenameRentalPointRequest;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointDetailsResponse;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rental-points")
public class AdminRentalPointController {

    private final FleetService fleetService;

    public AdminRentalPointController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalPointResponse createRentalPoint(
            @Valid @RequestBody CreateRentalPointRequest request
    ) {
        RentalPoint rentalPoint = fleetService.createRentalPoint(
                request.name(),
                request.locationNodeId()
        );

        return FleetWebMapper.toRentalPointResponse(rentalPoint);
    }

    @GetMapping
    public List<RentalPointResponse> getRentalPoints(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<RentalPoint> rentalPoints = activeOnly
                ? fleetService.findActiveRentalPoints()
                : fleetService.findAllRentalPoints();

        return rentalPoints.stream()
                .map(FleetWebMapper::toRentalPointResponse)
                .toList();
    }

    @GetMapping("/{rentalPointId}")
    public RentalPointDetailsResponse getRentalPointDetails(
            @PathVariable Long rentalPointId
    ) {
        RentalPoint rentalPoint = fleetService.getRentalPointById(rentalPointId);
        List<Scooter> scooters = fleetService.getRentalPointScooters(rentalPointId);

        return FleetWebMapper.toRentalPointDetailsResponse(rentalPoint, scooters);
    }

    @PatchMapping("/{rentalPointId}/rename")
    public RentalPointResponse renameRentalPoint(
            @PathVariable Long rentalPointId,
            @Valid @RequestBody RenameRentalPointRequest request
    ) {
        RentalPoint rentalPoint = fleetService.renameRentalPoint(
                rentalPointId,
                request.name()
        );

        return FleetWebMapper.toRentalPointResponse(rentalPoint);
    }

    @PatchMapping("/{rentalPointId}/activate")
    public RentalPointResponse activateRentalPoint(
            @PathVariable Long rentalPointId
    ) {
        RentalPoint rentalPoint = fleetService.activateRentalPoint(rentalPointId);

        return FleetWebMapper.toRentalPointResponse(rentalPoint);
    }

    @PatchMapping("/{rentalPointId}/deactivate")
    public RentalPointResponse deactivateRentalPoint(
            @PathVariable Long rentalPointId
    ) {
        RentalPoint rentalPoint = fleetService.deactivateRentalPoint(rentalPointId);

        return FleetWebMapper.toRentalPointResponse(rentalPoint);
    }

    @DeleteMapping("/{rentalPointId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRentalPoint(
            @PathVariable Long rentalPointId
    ) {
        fleetService.deleteRentalPoint(rentalPointId);
    }
}