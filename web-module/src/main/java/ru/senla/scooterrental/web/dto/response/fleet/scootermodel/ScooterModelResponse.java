package ru.senla.scooterrental.web.dto.response.fleet.scootermodel;

import ru.senla.scooterrental.common.enums.ScooterClass;

import java.math.BigDecimal;

public record ScooterModelResponse(

        Long id,

        ScooterClass scooterClass,

        double maxSpeedKmPerHour,

        double consumptionPerKm,

        BigDecimal pricePerMinute,

        BigDecimal pricePerHour,

        int batteryCapacity

) {
}
