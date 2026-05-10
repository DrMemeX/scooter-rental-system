package ru.senla.scooterrental.discount.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.discount.exceptions.DiscountValidationException;
import ru.senla.scooterrental.discount.repository.PromoCodeRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@Transactional
public class JpaPromoCodeRepository implements PromoCodeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PromoCode save(PromoCode promoCode) {
        validatePromoCode(promoCode);

        if (promoCode.getId() == null) {
            entityManager.persist(promoCode);
            return promoCode;
        }

        validateId(promoCode.getId(), "ID промокода");
        return entityManager.merge(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromoCode> findById(Long id) {
        validateId(id, "ID промокода");

        return Optional.ofNullable(
                entityManager.find(PromoCode.class, id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCode> findAll() {
        return entityManager
                .createQuery(
                        "select promoCode from PromoCode promoCode",
                        PromoCode.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id, "ID промокода");

        Long count = entityManager
                .createQuery(
                        "select count(promoCode) from PromoCode promoCode where promoCode.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID промокода");

        PromoCode promoCode = entityManager.find(PromoCode.class, id);

        if (promoCode != null) {
            entityManager.remove(promoCode);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromoCode> findByCode(String code) {
        String normalizedCode = normalizeCode(code);

        return entityManager
                .createQuery(
                        "select promoCode from PromoCode promoCode where promoCode.code = :code",
                        PromoCode.class
                )
                .setParameter("code", normalizedCode)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        String normalizedCode = normalizeCode(code);

        Long count = entityManager
                .createQuery(
                        "select count(promoCode) from PromoCode promoCode where promoCode.code = :code",
                        Long.class
                )
                .setParameter("code", normalizedCode)
                .getSingleResult();

        return count > 0;
    }

    private void validatePromoCode(PromoCode promoCode) {
        if (promoCode == null) {
            throw new DiscountValidationException(
                    "Промокод не может быть пустым"
            );
        }
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