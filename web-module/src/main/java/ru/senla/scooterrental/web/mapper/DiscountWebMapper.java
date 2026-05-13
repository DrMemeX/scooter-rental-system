package ru.senla.scooterrental.web.mapper;

import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.web.dto.response.discount.PromoCodeResponse;

public final class DiscountWebMapper {

    private DiscountWebMapper() {
    }

    public static PromoCodeResponse toResponse(PromoCode promoCode) {
        return new PromoCodeResponse(
                promoCode.getId(),
                promoCode.getCode(),
                promoCode.getPercent(),
                promoCode.getStatus()
        );
    }
}