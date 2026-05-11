package ru.senla.scooterrental.web.dto.request.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceRequest(

        @NotNull(message = "Сумма не может быть пустой")
        @DecimalMin(value = "0.01", message = "Сумма должна быть положительной")
        BigDecimal amount

) {
}