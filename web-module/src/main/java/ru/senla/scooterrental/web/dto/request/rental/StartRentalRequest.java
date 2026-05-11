package ru.senla.scooterrental.web.dto.request.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.senla.scooterrental.rental.enums.TariffType;

public record StartRentalRequest(

        @NotNull(message = "ID пользователя не может быть пустым")
        @Positive(message = "ID пользователя должен быть положительным")
        Long userId,

        @NotNull(message = "ID самоката не может быть пустым")
        @Positive(message = "ID самоката должен быть положительным")
        Long scooterId,

        @NotNull(message = "Тип тарифа не может быть пустым")
        TariffType tariffType,

        @Positive(message = "Количество часов должно быть положительным")
        Integer plannedHours

) {
}