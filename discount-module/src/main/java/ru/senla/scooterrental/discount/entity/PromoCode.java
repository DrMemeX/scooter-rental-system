package ru.senla.scooterrental.discount.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.senla.scooterrental.discount.enums.PromoCodeStatus;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;

import java.math.BigDecimal;
import java.util.Locale;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PromoCodeStatus status;

    protected PromoCode() {
    }

    public PromoCode(String code, BigDecimal percent) {
        this.code = requireNotBlank(code, "Код промокода").toUpperCase(Locale.ROOT);
        this.percent = requireValidPercent(percent);
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

    public BigDecimal getPercent() {
        return percent;
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

    private BigDecimal requireValidPercent(BigDecimal percent) {
        requireNonNull(percent, "Процент скидки");

        if (percent.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DiscountValidationException(
                    "Процент скидки должен быть положительным"
            );
        }

        if (percent.compareTo(BigDecimal.valueOf(15)) > 0) {
            throw new DiscountValidationException(
                    "Процент скидки не может быть больше 15"
            );
        }

        return percent;
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