package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.user.entity.User;

public interface RentalCalculationService {

    long calculateActualMinutes(Rental rental);

    long resolveEffectiveMinutes(Rental rental, long actualMinutes);

    double calculateChargeConsumption(Scooter scooter, double distanceKm);

    int calculateMaxAllowedMinutes(User user, Scooter scooter);

    int calculateAvailableMinutesByBattery(Scooter scooter);

    int calculateAvailableMinutesByMoneyForHour(User user, Scooter scooter);

    Integer calculateMaxAllowedMinutesForStart(
            User user,
            Scooter scooter,
            TariffType tariffType,
            Integer plannedHours
    );
}