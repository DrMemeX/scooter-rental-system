package ru.senla.scooterrental.discount.repository.impl;

import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.repository.PromoCodeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryPromoCodeRepository implements PromoCodeRepository {

    private final List<PromoCode> promoCodes = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public PromoCode save(PromoCode promoCode) {
        if (promoCode == null) {
            throw new DiscountValidationException(
                    "Промокод не может быть пустым"
            );
        }

        if (promoCode.getId() == null) {
            promoCode.assignId(nextId++);
            promoCodes.add(promoCode);
            return promoCode;
        }

        for (int i = 0; i < promoCodes.size(); i++) {
            if (promoCodes.get(i).getId().equals(promoCode.getId())) {
                promoCodes.set(i, promoCode);
                return promoCode;
            }
        }

        throw new DiscountValidationException(
                "Невозможно обновить промокод: объект с ID " + promoCode.getId() + " не найден"
        );
    }

    @Override
    public Optional<PromoCode> findById(Long id) {
        validateId(id, "ID промокода");

        return promoCodes.stream()
                .filter(promoCode -> promoCode.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<PromoCode> findAll() {
        return new ArrayList<>(promoCodes);
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id, "ID промокода");

        return promoCodes.stream()
                .anyMatch(promoCode -> promoCode.getId().equals(id));
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID промокода");

        promoCodes.removeIf(promoCode -> promoCode.getId().equals(id));
    }

    @Override
    public Optional<PromoCode> findByCode(String code) {
        String normalizedCode = normalizeCode(code);

        return promoCodes.stream()
                .filter(promoCode -> promoCode.getCode().equals(normalizedCode))
                .findFirst();
    }

    @Override
    public boolean existsByCode(String code) {
        String normalizedCode = normalizeCode(code);

        return promoCodes.stream()
                .anyMatch(promoCode -> promoCode.getCode().equals(normalizedCode));
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new DiscountValidationException(
                    fieldName + " должен быть положительным"
            );
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DiscountValidationException(
                    "Код промокода не может быть пустым"
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}