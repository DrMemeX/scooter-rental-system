package ru.senla.scooterrental.web.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.ScooterModelService;
import ru.senla.scooterrental.web.dto.response.fleet.scootermodel.ScooterModelResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scooter-models")
public class UserScooterModelController {

    private final ScooterModelService scooterModelService;

    public UserScooterModelController(ScooterModelService scooterModelService) {
        this.scooterModelService = scooterModelService;
    }

    @GetMapping
    public List<ScooterModelResponse> getScooterModels() {
        return scooterModelService.findAllScooterModels()
                .stream()
                .map(FleetWebMapper::toScooterModelResponse)
                .toList();
    }

    @GetMapping("/{modelId}")
    public ScooterModelResponse getScooterModelById(
            @PathVariable("modelId") Long modelId
    ) {
        ScooterModel model = scooterModelService.getScooterModelById(modelId);

        return FleetWebMapper.toScooterModelResponse(model);
    }
}