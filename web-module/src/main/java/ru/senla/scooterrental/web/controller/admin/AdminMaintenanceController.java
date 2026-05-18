package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.service.MaintenanceService;
import ru.senla.scooterrental.web.dto.request.maintenance.ChargeScooterRequest;
import ru.senla.scooterrental.web.dto.request.maintenance.MaintenanceEventRequest;
import ru.senla.scooterrental.web.dto.response.maintenance.MaintenanceEventResponse;
import ru.senla.scooterrental.web.mapper.MaintenanceWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/maintenance")
public class AdminMaintenanceController {

    private final MaintenanceService maintenanceService;

    public AdminMaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/scooters/{scooterId}/technical-breakdown")
    public MaintenanceEventResponse reportTechnicalBreakdown(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MaintenanceEventRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.reportTechnicalBreakdown(
                scooterId,
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @PostMapping("/scooters/{scooterId}/user-damage")
    public MaintenanceEventResponse reportUserDamage(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MaintenanceEventRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.reportUserDamage(
                scooterId,
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @PostMapping("/scooters/{scooterId}/send")
    public MaintenanceEventResponse sendToMaintenance(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MaintenanceEventRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.sendToMaintenance(
                scooterId,
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @PostMapping("/scooters/{scooterId}/complete")
    public MaintenanceEventResponse completeMaintenance(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MaintenanceEventRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.completeMaintenance(
                scooterId,
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @PostMapping("/scooters/{scooterId}/charge")
    public MaintenanceEventResponse chargeScooter(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody ChargeScooterRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.chargeScooter(
                scooterId,
                request.amount(),
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @PostMapping("/scooters/{scooterId}/service-required")
    public MaintenanceEventResponse markServiceRequired(
            @PathVariable("scooterId") Long scooterId,
            @Valid @RequestBody MaintenanceEventRequest request
    ) {
        ScooterServiceEvent event = maintenanceService.markServiceRequired(
                scooterId,
                request.description()
        );

        return MaintenanceWebMapper.toResponse(event);
    }

    @GetMapping("/events")
    public List<MaintenanceEventResponse> getEvents(
            @RequestParam(value = "type", required = false) ServiceEventType type
    ) {
        List<ScooterServiceEvent> events = type == null
                ? maintenanceService.getAllEvents()
                : maintenanceService.getEventsByType(type);

        return events.stream()
                .map(MaintenanceWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/scooters/{scooterId}/events")
    public List<MaintenanceEventResponse> getEventsByScooterId(
            @PathVariable("scooterId") Long scooterId
    ) {
        return maintenanceService.getEventsByScooterId(scooterId)
                .stream()
                .map(MaintenanceWebMapper::toResponse)
                .toList();
    }
}