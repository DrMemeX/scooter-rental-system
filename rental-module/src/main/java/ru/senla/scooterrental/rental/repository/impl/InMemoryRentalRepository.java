package ru.senla.scooterrental.rental.repository.impl;

import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryRentalRepository implements RentalRepository {

    private final List<Rental> rentals = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public Rental save(Rental rental) {
        if (rental == null) {
            throw new RentalValidationException(
                    "Аренда не может быть пустой"
            );
        }

        if (rental.getId() == null) {
            rental.assignId(nextId++);
            rentals.add(rental);
            return rental;
        }

        for (int i = 0; i < rentals.size(); i++) {
            if (rentals.get(i).getId().equals(rental.getId())) {
                rentals.set(i, rental);
                return rental;
            }
        }

        throw new RentalNotFoundException(
                "Невозможно обновить аренду: объект с id " + rental.getId() + " не найден"
        );
    }

    @Override
    public Optional<Rental> findById(Long id) {
        validateId(id, "ID аренды");

        return rentals.stream()
                .filter(rental -> rental.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Rental> findAll() {
        return new ArrayList<>(rentals);
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id, "ID аренды");

        return rentals.stream()
                .anyMatch(rental -> rental.getId().equals(id));
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID аренды");

        rentals.removeIf(rental -> rental.getId().equals(id));
    }

    @Override
    public List<Rental> findByUserId(Long userId) {
        validateId(userId, "ID пользователя");

        return rentals.stream()
                .filter(rental -> rental.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<Rental> findByScooterId(Long scooterId) {
        validateId(scooterId, "ID самоката");

        return rentals.stream()
                .filter(rental -> rental.getScooterId().equals(scooterId))
                .toList();
    }

    @Override
    public Optional<Rental> findActiveByUserId(Long userId) {
        validateId(userId, "ID пользователя");

        return rentals.stream()
                .filter(rental -> rental.getUserId().equals(userId))
                .filter(rental -> rental.getStatus() == RentalStatus.ACTIVE)
                .findFirst();
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new RentalValidationException(
                    fieldName + " должен быть положительным"
            );
        }
    }
}