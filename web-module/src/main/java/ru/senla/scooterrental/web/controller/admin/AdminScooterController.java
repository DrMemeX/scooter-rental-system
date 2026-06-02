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
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.service.ScooterService;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.CreateScooterRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.MoveScooterRequest;
import ru.senla.scooterrental.web.dto.response.fleet.scooter.ScooterResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/scooters")
public class AdminScooterController {

    private final ScooterService scooterService;

    public AdminScooterController(ScooterService scooterService) {
        this.scooterService = scooterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScooterResponse createScooter(
            @Valid @RequestBody CreateScooterRequest request
    ) {
        Scooter scooter = scooterService.createScooter(
                request.modelId(),
                request.rentalPointId(),
                request.initialCharge()
        );

        return FleetWebMapper.toScooterResponse(scooter);
    }

    @GetMapping
    public List<ScooterResponse> getScooters(
            @RequestParam(value = "status", required = false) ScooterStatus status,
            @RequestParam(value = "rentalPointId", required = false) Long rentalPointId,
            @RequestParam(value = "availableOnly", required = false, defaultValue = "false") boolean availableOnly
    ) {
        List<Scooter> scooters;

        if (availableOnly) {
            scooters = scooterService.findAvailableScooters();
        } else if (status != null) {
            scooters = scooterService.findScootersByStatus(status);
        } else if (rentalPointId != null) {
            scooters = scooterService.findScootersByRentalPoint(rentalPointId);
        } else {
            scooters = scooterService.findAllScooters();
        }

        return scooters.stream()
                .map(FleetWebMapper::toScooterResponse)
                .toList();
    }

    @GetMapping("/{scooterId}")
    public ScooterResponse getScooterById(
            @PathVariable("scooterId") Long scooterId
    ) {
        Scooter scooter = scooterService.getScooterById(scooterId);

        return FleetWebMapper.toScooterResponse(scooter);
    }

    @PatchMapping("/{scooterId}/move")
    public ScooterResponse moveScooter(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MoveScooterRequest request
    ) {
        Scooter scooter = scooterService.moveScooterToRentalPoint(
                scooterId,
                request.rentalPointId()
        );

        return FleetWebMapper.toScooterResponse(scooter);
    }

    @DeleteMapping("/{scooterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScooter(
            @PathVariable("scooterId") Long scooterId
    ) {
        scooterService.deleteScooter(scooterId);
    }
}