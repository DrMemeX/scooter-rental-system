package ru.senla.scooterrental.discount.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.repository.PromoCodeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class DiscountService {

    private static final Logger log =
            LoggerFactory.getLogger(DiscountService.class);

    private final PromoCodeRepository promoCodeRepository;

    public DiscountService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = requireNonNull(promoCodeRepository, "Репозиторий промокодов");
    }

    public PromoCode createPromoCode(PromoCode promoCode) {
        requireNonNull(promoCode, "Промокод");

        log.info("Creating promo code: code={}", promoCode.getCode());

        if (promoCodeRepository.existsByCode(promoCode.getCode())) {
            log.warn("Promo code creation rejected: code={} already exists", promoCode.getCode());

            throw new DiscountValidationException(
                    "Промокод уже существует"
            );
        }

        PromoCode savedPromoCode = promoCodeRepository.save(promoCode);

        log.info(
                "Promo code created successfully: promoCodeId={}, code={}",
                savedPromoCode.getId(),
                savedPromoCode.getCode()
        );

        return savedPromoCode;
    }

    @Transactional(readOnly = true)
    public PromoCode getByCodeOrThrow(String code) {
        String normalizedCode = requireNotBlank(code, "Код промокода");

        return promoCodeRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new DiscountValidationException(
                        "Промокод не найден"
                ));
    }

    @Transactional(readOnly = true)
    public List<PromoCode> getAllPromoCodes() {
        return promoCodeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PromoCode getPromoCodeById(Long promoCodeId) {
        if (promoCodeId == null || promoCodeId <= 0) {
            throw new DiscountValidationException(
                    "ID промокода должен быть положительным"
            );
        }

        return promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new DiscountValidationException(
                        "Промокод с ID " + promoCodeId + " не найден"
                ));
    }

    @Transactional(readOnly = true)
    public BigDecimal applyDiscount(BigDecimal price, String code) {
        BigDecimal validPrice = requireNonNegative(price, "Цена");

        if (code == null) {
            log.info("Discount skipped: promo code is not provided, price={}", validPrice);

            return validPrice;
        }

        String normalizedCode = requireNotBlank(code, "Код промокода");

        log.info(
                "Applying discount: code={}, initialPrice={}",
                normalizedCode,
                validPrice
        );

        PromoCode promoCode = getByCodeOrThrow(normalizedCode);

        if (!promoCode.isActive()) {
            log.warn("Discount rejected: promo code is inactive, code={}", normalizedCode);

            throw new DiscountValidationException(
                    "Промокод не активен"
            );
        }

        BigDecimal percent = requireNonNegative(
                promoCode.getPercent(),
                "Процент скидки"
        );

        BigDecimal discount = validPrice
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalPrice = validPrice.subtract(discount)
                .max(BigDecimal.ZERO);

        log.info(
                "Discount applied successfully: code={}, percent={}, initialPrice={}, finalPrice={}",
                normalizedCode,
                percent,
                validPrice,
                finalPrice
        );

        return finalPrice;
    }

    public void deactivate(String code) {
        log.info("Deactivating promo code: code={}", code);

        PromoCode promoCode = getByCodeOrThrow(code);
        promoCode.deactivate();
        promoCodeRepository.save(promoCode);

        log.info("Promo code deactivated successfully: code={}", promoCode.getCode());
    }

    public void activate(String code) {
        log.info("Activating promo code: code={}", code);

        PromoCode promoCode = getByCodeOrThrow(code);
        promoCode.activate();
        promoCodeRepository.save(promoCode);

        log.info("Promo code activated successfully: code={}", promoCode.getCode());
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