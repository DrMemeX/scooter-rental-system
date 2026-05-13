package ru.senla.scooterrental.web.dto.response.fleet.scooter;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;

import java.math.BigDecimal;

public record ScooterShortResponse(

        Long id,

        ScooterStatus status,

        Double currentCharge,

        Double totalMileageKm,

        Long modelId,

        ScooterClass scooterClass,

        BigDecimal pricePerMinute,

        BigDecimal pricePerHour

) {
}