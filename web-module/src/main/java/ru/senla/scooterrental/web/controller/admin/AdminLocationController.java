package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.service.LocationService;
import ru.senla.scooterrental.web.dto.request.fleet.location.CreateLocationRequest;
import ru.senla.scooterrental.web.dto.request.fleet.location.RenameLocationRequest;
import ru.senla.scooterrental.web.dto.response.fleet.location.LocationResponse;
import ru.senla.scooterrental.web.mapper.FleetWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/locations")
public class AdminLocationController {

    private final LocationService locationService;

    public AdminLocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse createLocation(
            @Valid @RequestBody CreateLocationRequest request
    ) {
        LocationNode location = locationService.createLocation(
                request.name(),
                request.type(),
                request.parentId()
        );

        return FleetWebMapper.toLocationResponse(location);
    }

    @GetMapping
    public List<LocationResponse> getLocations(
            @RequestParam(value = "type", required = false) LocationType type
    ) {
        List<LocationNode> locations = type == null
                ? locationService.findAllLocations()
                : locationService.findLocationsByType(type);

        return locations.stream()
                .map(FleetWebMapper::toLocationResponse)
                .toList();
    }

    @GetMapping("/{locationId}/children")
    public List<LocationResponse> getChildLocations(
            @PathVariable("locationId") Long locationId,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<LocationNode> locations = activeOnly
                ? locationService.findActiveChildLocations(locationId)
                : locationService.findChildLocations(locationId);

        return locations.stream()
                .map(FleetWebMapper::toLocationResponse)
                .toList();
    }

    @PatchMapping("/{locationId}/rename")
    public LocationResponse renameLocation(
            @PathVariable("locationId") Long locationId,
            @Valid @RequestBody RenameLocationRequest request
    ) {
        LocationNode location = locationService.renameLocation(
                locationId,
                request.name()
        );

        return FleetWebMapper.toLocationResponse(location);
    }

    @PatchMapping("/{locationId}/activate")
    public LocationResponse activateLocation(
            @PathVariable("locationId") Long locationId
    ) {
        LocationNode location = locationService.activateLocation(locationId);

        return FleetWebMapper.toLocationResponse(location);
    }

    @PatchMapping("/{locationId}/deactivate")
    public LocationResponse deactivateLocation(
            @PathVariable("locationId") Long locationId
    ) {
        LocationNode location = locationService.deactivateLocation(locationId);

        return FleetWebMapper.toLocationResponse(location);
    }
}