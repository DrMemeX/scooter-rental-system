package ru.senla.scooterrental.web.mapper;

import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.web.dto.response.rental.RentalResponse;

public final class RentalWebMapper {

    private RentalWebMapper() {
    }

    public static RentalResponse toResponse(Rental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getUserId(),
                rental.getScooterId(),
                rental.getStatus(),
                rental.getStartTime(),
                rental.getEndTime(),
                rental.getTariffType(),
                rental.getPlannedHours(),
                rental.getMaxAllowedMinutes(),
                rental.getTotalCost(),
                rental.getPromoCodeId(),
                rental.getDiscountAmount(),
                rental.getTerminationReason(),
                rental.getDistanceKm()
        );
    }
}