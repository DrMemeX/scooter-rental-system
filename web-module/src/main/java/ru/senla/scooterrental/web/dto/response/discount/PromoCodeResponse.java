package ru.senla.scooterrental.web.dto.response.discount;

import ru.senla.scooterrental.discount.enums.PromoCodeStatus;

import java.math.BigDecimal;

public record PromoCodeResponse(

        Long id,

        String code,

        BigDecimal percent,

        PromoCodeStatus status

) {
}