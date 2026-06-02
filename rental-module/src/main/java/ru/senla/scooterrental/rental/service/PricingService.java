package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TerminationReason;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal calculate(Rental rental, Scooter scooter);

    BigDecimal calculate(Rental rental,
                         Scooter scooter,
                         TerminationReason reason,
                         long effectiveMinutes
    );
}
