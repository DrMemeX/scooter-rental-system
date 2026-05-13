package ru.senla.scooterrental.web.dto.request.discount;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePromoCodeRequest(

        @NotBlank(message = "Код промокода не может быть пустым")
        @Size(max = 50, message = "Код промокода слишком длинный")
        String code,

        @NotNull(message = "Процент скидки не может быть пустым")
        @DecimalMin(value = "0.01", message = "Процент скидки должен быть положительным")
        @DecimalMax(value = "15.00", message = "Процент скидки не может быть больше 15")
        BigDecimal percent

) {
}