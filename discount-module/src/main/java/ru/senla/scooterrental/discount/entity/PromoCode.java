package ru.senla.scooterrental.discount.entity;

import ru.senla.scooterrental.discount.enums.DiscountType;
import ru.senla.scooterrental.discount.enums.PromoCodeStatus;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;

import java.util.Locale;

public class PromoCode {

    private Long id;

    private final String code;
    private final DiscountType discountType;

    private PromoCodeStatus status;

    public PromoCode(String code, DiscountType discountType) {
        this.code = requireNotBlank(code, "Код промокода").toUpperCase(Locale.ROOT);
        this.discountType = requireNonNull(discountType, "Тип скидки");
        this.status = PromoCodeStatus.ACTIVE;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new DiscountValidationException(
                    "ID промокода уже назначен"
            );
        }

        this.id = requirePositiveId(id, "ID промокода");
    }

    public void activate() {
        this.status = PromoCodeStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = PromoCodeStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == PromoCodeStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public PromoCodeStatus getStatus() {
        return status;
    }

    private String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new DiscountValidationException(
                    name + " не может быть пустым"
            );
        }

        return value.trim();
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new DiscountValidationException(
                    name + " не задан"
            );
        }

        return obj;
    }

    private Long requirePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new DiscountValidationException(
                    name + " должен быть положительным"
            );
        }

        return id;
    }
}