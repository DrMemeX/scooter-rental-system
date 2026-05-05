package ru.senla.scooterrental.rental.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.rental.entity.Rental;

import java.util.List;
import java.util.Optional;

public interface RentalRepository extends CrudRepository<Rental, Long> {

    List<Rental> findByUserId(Long userId);

    List<Rental> findByScooterId(Long scooterId);

    Optional<Rental> findActiveByUserId(Long userId);
}