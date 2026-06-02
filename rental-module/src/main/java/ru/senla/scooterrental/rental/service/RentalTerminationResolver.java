package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.user.entity.User;

public interface RentalTerminationResolver {

    TerminationReason resolveTerminationReason(
            Rental rental,
            User user,
            Scooter scooter,
            long actualMinutes,
            long effectiveMinutes,
            TerminationReason requestedReason
    );
}