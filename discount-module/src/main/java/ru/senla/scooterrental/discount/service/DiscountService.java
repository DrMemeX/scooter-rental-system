package ru.senla.scooterrental.discount.service;

import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.repository.PromoCodeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class DiscountService {

    private final PromoCodeRepository promoCodeRepository;

    public DiscountService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = requireNonNull(promoCodeRepository, "Репозиторий промокодов");
    }

    public PromoCode createPromoCode(PromoCode promoCode) {
        requireNonNull(promoCode, "Промокод");

        if (promoCodeRepository.existsByCode(promoCode.getCode())) {
            throw new DiscountValidationException(
                    "Промокод уже существует"
            );
        }

        return promoCodeRepository.save(promoCode);
    }

    public PromoCode getByCodeOrThrow(String code) {
        String normalizedCode = requireNotBlank(code, "Код промокода");

        return promoCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new DiscountValidationException(
                        "Промокод не найден"
                ));
    }

    public BigDecimal applyDiscount(BigDecimal price, String code) {
        BigDecimal validPrice = requireNonNegative(price, "Цена");

        if (code == null || code.isBlank()) {
            return validPrice;
        }

        PromoCode promoCode = getByCodeOrThrow(code);

        if (!promoCode.isActive()) {
            throw new DiscountValidationException(
                    "Промокод не активен"
            );
        }

        BigDecimal percent = requireNonNegative(
                promoCode.getDiscountType().getPercent(),
                "Процент скидки"
        );

        BigDecimal discount = validPrice
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = validPrice.subtract(discount);

        return finalPrice.max(BigDecimal.ZERO);
    }

    public void deactivate(String code) {
        PromoCode promoCode = getByCodeOrThrow(code);
        promoCode.deactivate();
        promoCodeRepository.save(promoCode);
    }

    public void activate(String code) {
        PromoCode promoCode = getByCodeOrThrow(code);
        promoCode.activate();
        promoCodeRepository.save(promoCode);
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new DiscountValidationException(
                    name + " не задан"
            );
        }

        return obj;
    }

    private BigDecimal requireNonNegative(BigDecimal value, String name) {
        requireNonNull(value, name);

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new DiscountValidationException(
                    name + " не может быть отрицательным"
            );
        }

        return value;
    }

    private String requireNotBlank(String value, String name) {
        requireNonNull(value, name);

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            throw new DiscountValidationException(
                    name + " не может быть пустым"
            );
        }

        return trimmedValue.toUpperCase(Locale.ROOT);
    }
}