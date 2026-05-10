package ru.senla.scooterrental.discount.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.senla.scooterrental.discount.enums.DiscountType;
import ru.senla.scooterrental.discount.enums.PromoCodeStatus;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;

import java.util.Locale;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 50)
    private DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PromoCodeStatus status;

    protected PromoCode() {
    }

    public PromoCode(String code, DiscountType discountType) {
        this.code = requireNotBlank(code, "Код промокода").toUpperCase(Locale.ROOT);
        this.discountType = requireNonNull(discountType, "Тип скидки");
        this.status = PromoCodeStatus.ACTIVE;
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
}