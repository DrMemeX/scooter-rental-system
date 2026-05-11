package ru.senla.scooterrental.web.dto.request.rental;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FinishRentalRequest(

        @Positive(message = "ID точки проката должен быть положительным")
        Long rentalPointId,

        @PositiveOrZero(message = "Дистанция не может быть отрицательной")
        double distanceKm,

        String promoCode

) {
}