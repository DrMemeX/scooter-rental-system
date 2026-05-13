package ru.senla.scooterrental.discount.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.discount.entity.PromoCode;

import java.util.Optional;

public interface PromoCodeRepository extends CrudRepository<PromoCode, Long> {

    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(String code);
}