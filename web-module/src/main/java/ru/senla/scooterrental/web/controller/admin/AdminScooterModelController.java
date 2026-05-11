package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.CreateScooterModelRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.UpdateScooterModelPricesRequest;
import ru.senla.scooterrental.web.dto.response.fleet.scootermodel.ScooterModelResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/scooter-models")
public class AdminScooterModelController {

    private final FleetService fleetService;

    public AdminScooterModelController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScooterModelResponse createScooterModel(
            @Valid @RequestBody CreateScooterModelRequest request
    ) {
        ScooterModel model = fleetService.createScooterModel(
                request.scooterClass(),
                request.maxSpeedKmPerHour(),
                request.consumptionPerKm(),
                request.pricePerMinute(),
                request.pricePerHour(),
                request.batteryCapacity()
        );

        return FleetWebMapper.toScooterModelResponse(model);
    }

    @GetMapping
    public List<ScooterModelResponse> getScooterModels() {
        return fleetService.findAllScooterModels()
                .stream()
                .map(FleetWebMapper::toScooterModelResponse)
                .toList();
    }

    @GetMapping("/{modelId}")
    public ScooterModelResponse getScooterModelById(
            @PathVariable Long modelId
    ) {
        ScooterModel model = fleetService.getScooterModelById(modelId);

        return FleetWebMapper.toScooterModelResponse(model);
    }

    @PatchMapping("/{modelId}/prices")
    public ScooterModelResponse updateScooterModelPrices(
            @PathVariable Long modelId,
            @Valid @RequestBody UpdateScooterModelPricesRequest request
    ) {
        ScooterModel model = fleetService.updateScooterModelPrices(
                modelId,
                request.pricePerMinute(),
                request.pricePerHour()
        );

        return FleetWebMapper.toScooterModelResponse(model);
    }
}