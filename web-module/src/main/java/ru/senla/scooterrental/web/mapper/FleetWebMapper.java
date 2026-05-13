package ru.senla.scooterrental.web.mapper;

import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.web.dto.response.fleet.location.LocationResponse;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointDetailsResponse;
import ru.senla.scooterrental.web.dto.response.fleet.rentalpoint.RentalPointResponse;
import ru.senla.scooterrental.web.dto.response.fleet.scooter.ScooterResponse;
import ru.senla.scooterrental.web.dto.response.fleet.scootermodel.ScooterModelResponse;
import ru.senla.scooterrental.web.dto.response.fleet.scooter.ScooterShortResponse;

import java.util.List;

public final class FleetWebMapper {

    private FleetWebMapper() {
    }

    public static LocationResponse toLocationResponse(LocationNode location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getType(),
                location.getParent() == null ? null : location.getParent().getId(),
                location.isActive()
        );
    }

    public static RentalPointResponse toRentalPointResponse(RentalPoint rentalPoint) {
        LocationNode location = rentalPoint.getLocationNode();

        return new RentalPointResponse(
                rentalPoint.getId(),
                rentalPoint.getName(),
                rentalPoint.isActive(),
                location.getId(),
                location.getName(),
                location.getType()
        );
    }

    public static RentalPointDetailsResponse toRentalPointDetailsResponse(
            RentalPoint rentalPoint,
            List<Scooter> scooters
    ) {
        return new RentalPointDetailsResponse(
                toRentalPointResponse(rentalPoint),
                scooters.size(),
                scooters.stream()
                        .map(FleetWebMapper::toScooterShortResponse)
                        .toList()
        );
    }

    public static ScooterShortResponse toScooterShortResponse(Scooter scooter) {
        ScooterModel model = scooter.getModel();

        return new ScooterShortResponse(
                scooter.getId(),
                scooter.getStatus(),
                scooter.getCurrentCharge(),
                scooter.getTotalMileageKm(),
                model.getId(),
                model.getScooterClass(),
                model.getPricePerMinute(),
                model.getPricePerHour()
        );
    }

    public static ScooterResponse toScooterResponse(Scooter scooter) {
        ScooterModel model = scooter.getModel();
        RentalPoint rentalPoint = scooter.getCurrentRentalPoint();

        return new ScooterResponse(
                scooter.getId(),
                scooter.getStatus(),
                scooter.getCurrentCharge(),
                scooter.getTotalMileageKm(),
                model.getId(),
                model.getScooterClass(),
                model.getMaxSpeedKmPerHour(),
                model.getConsumptionPerKm(),
                model.getPricePerMinute(),
                model.getPricePerHour(),
                model.getBatteryCapacity(),
                rentalPoint == null ? null : rentalPoint.getId(),
                rentalPoint == null ? null : rentalPoint.getName()
        );
    }

    public static ScooterModelResponse toScooterModelResponse(ScooterModel model) {
        return new ScooterModelResponse(
                model.getId(),
                model.getScooterClass(),
                model.getMaxSpeedKmPerHour(),
                model.getConsumptionPerKm(),
                model.getPricePerMinute(),
                model.getPricePerHour(),
                model.getBatteryCapacity()
        );
    }
}
