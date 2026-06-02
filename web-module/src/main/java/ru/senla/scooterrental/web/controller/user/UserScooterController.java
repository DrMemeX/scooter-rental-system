package ru.senla.scooterrental.web.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.service.ScooterService;
import ru.senla.scooterrental.web.dto.response.fleet.scooter.ScooterResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scooters")
public class UserScooterController {

    private final ScooterService scooterService;

    public UserScooterController(ScooterService scooterService) {
        this.scooterService = scooterService;
    }

    @GetMapping("/available")
    public List<ScooterResponse> getAvailableScooters() {
        return scooterService.findAvailableScooters()
                .stream()
                .map(FleetWebMapper::toScooterResponse)
                .toList();
    }
}