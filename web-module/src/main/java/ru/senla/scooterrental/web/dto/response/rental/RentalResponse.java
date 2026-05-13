package ru.senla.scooterrental.web.dto.response.rental;

import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RentalResponse(

        Long id,

        Long userId,

        Long scooterId,

        RentalStatus status,

        LocalDateTime startTime,

        LocalDateTime endTime,

        TariffType tariffType,

        Integer plannedHours,

        Integer maxAllowedMinutes,

        BigDecimal totalCost,

        Long promoCodeId,

        BigDecimal discountAmount,

        TerminationReason terminationReason,

        double distanceKm

) {
}