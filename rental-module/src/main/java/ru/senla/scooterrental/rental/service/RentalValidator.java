package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.user.entity.User;

public interface RentalValidator {

    void validatePositiveId(Long id, String name);

    void ensureUserCanStartRental(User user);

    void ensureCanPayForRental(
            User user,
            Scooter scooter,
            TariffType tariffType,
            Integer plannedHours
    );

    void validatePromoCodeNotUsedByUser(Long userId, String promoCode);

    void validateRideDistance(
            Scooter scooter,
            double distanceKm,
            long actualMinutes
    );

    void validateActiveRental(Rental rental);

    void validatePendingManualFinish(Rental rental);

    <T> T requireNonNull(T obj, String name);
}