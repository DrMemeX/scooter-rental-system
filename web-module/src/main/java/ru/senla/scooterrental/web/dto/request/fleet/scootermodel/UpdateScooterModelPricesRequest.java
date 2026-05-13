package ru.senla.scooterrental.web.dto.request.fleet.scootermodel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateScooterModelPricesRequest(

        @NotNull(message = "Цена за минуту не может быть пустой")
        @DecimalMin(value = "0.01", message = "Цена за минуту должна быть положительной")
        BigDecimal pricePerMinute,

        @NotNull(message = "Цена за час не может быть пустой")
        @DecimalMin(value = "0.01", message = "Цена за час должна быть положительной")
        BigDecimal pricePerHour

) {
}