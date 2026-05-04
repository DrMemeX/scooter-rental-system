package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class PricingService {

    public BigDecimal calculate(Rental rental, Scooter scooter) {
        if (rental == null) {
            throw new RentalValidationException(
                    "Аренда не может быть пустой"
            );
        }

        if (scooter == null) {
            throw new RentalValidationException(
                    "Самокат не может быть пустым"
            );
        }

        if (scooter.getModel() == null) {
            throw new RentalValidationException(
                    "Модель самоката не может быть пустой"
            );
        }

        long minutes = calculateRentalMinutes(rental.getStartTime());

        if (rental.getTariffType() == null) {
            throw new RentalValidationException(
                    "Тип тарифа не может быть пустым"
            );
        }

        return switch (rental.getTariffType()) {
            case MINUTE -> calculateMinutePrice(minutes, scooter);
            case HOUR -> calculateHourPrice(minutes, scooter);
            case SUBSCRIPTION -> BigDecimal.ZERO;
            default -> throw new RentalValidationException(
                    "Неизвестный тип тарифа"
            );
        };
    }

    private long calculateRentalMinutes(LocalDateTime startTime) {
        if (startTime == null) {
            throw new RentalValidationException(
                    "Время начала аренды не может быть пустым"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (startTime.isAfter(now)) {
            throw new RentalValidationException(
                    "Время начала аренды не может быть в будущем"
            );
        }

        long minutes = Duration.between(startTime, now).toMinutes();

        return Math.max(minutes, 1);
    }

    private BigDecimal calculateMinutePrice(long minutes, Scooter scooter) {
        return scooter.getModel()
                .getPricePerMinute()
                .multiply(BigDecimal.valueOf(minutes));
    }

    private BigDecimal calculateHourPrice(long minutes, Scooter scooter) {
        long hours = (long) Math.ceil(minutes / 60.0);

        return scooter.getModel()
                .getPricePerHour()
                .multiply(BigDecimal.valueOf(hours));
    }
}