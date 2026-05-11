package ru.senla.scooterrental.web.dto.request.fleet.scootermodel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.senla.scooterrental.common.enums.ScooterClass;

import java.math.BigDecimal;

public record CreateScooterModelRequest(

        @NotNull(message = "Класс самоката не может быть пустым")
        ScooterClass scooterClass,

        @Positive(message = "Максимальная скорость должна быть положительной")
        double maxSpeedKmPerHour,

        @Positive(message = "Расход заряда должен быть положительным")
        double consumptionPerKm,

        @NotNull(message = "Цена за минуту не может быть пустой")
        @DecimalMin(value = "0.01", message = "Цена за минуту должна быть положительной")
        BigDecimal pricePerMinute,

        @NotNull(message = "Цена за час не может быть пустой")
        @DecimalMin(value = "0.01", message = "Цена за час должна быть положительной")
        BigDecimal pricePerHour,

        @Positive(message = "Емкость батареи должна быть положительной")
        int batteryCapacity

) {
}