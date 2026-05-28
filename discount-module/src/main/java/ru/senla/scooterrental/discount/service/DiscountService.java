package ru.senla.scooterrental.discount.service;

import ru.senla.scooterrental.discount.entity.PromoCode;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {

    PromoCode createPromoCode(
            PromoCode promoCode
    );

    PromoCode getByCodeOrThrow(
            String code
    );

    List<PromoCode> getAllPromoCodes();

    PromoCode getPromoCodeById(
            Long promoCodeId
    );

    BigDecimal applyDiscount(
            BigDecimal price,
            String code
    );

    void deactivate(
            String code
    );

    void activate(
            String code
    );
}