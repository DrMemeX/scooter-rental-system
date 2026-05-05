package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class PricingService {

    public BigDecimal calculate(Rental rental, Scooter scooter) {

        requireNonNull(rental, "Аренда");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");
        requireNonNull(rental.getTariffType(), "Тип тарифа");

        long minutes = calculateRentalMinutes(rental.getStartTime());

        return switch (rental.getTariffType()) {
            case MINUTE -> calculateMinutePrice(minutes, rental, scooter);
            case HOUR -> calculateHourPrice(rental, scooter);
            case SUBSCRIPTION -> BigDecimal.ZERO;
        };
    }

    private long calculateRentalMinutes(LocalDateTime startTime) {
        requireNonNull(startTime, "Время начала аренды");

        LocalDateTime now = LocalDateTime.now();

        if (startTime.isAfter(now)) {
            throw new RentalValidationException(
                    "Время начала аренды не может быть в будущем"
            );
        }

        long minutes = Duration.between(startTime, now).toMinutes();

        return Math.max(minutes, 1);
    }

    private BigDecimal calculateMinutePrice(long actualMinutes,
                                            Rental rental,
                                            Scooter scooter) {
        Integer maxAllowedMinutes = rental.getMaxAllowedMinutes();

        if (maxAllowedMinutes == null || maxAllowedMinutes <= 0) {
            throw new RentalValidationException(
                    "Для поминутного тарифа не задан лимит оплаченного времени"
            );
        }

        long billableMinutes = Math.min(actualMinutes, maxAllowedMinutes);

        return scooter.getModel()
                .getPricePerMinute()
                .multiply(BigDecimal.valueOf(billableMinutes));
    }

    private BigDecimal calculateHourPrice(Rental rental, Scooter scooter) {
        Integer plannedHours = rental.getPlannedHours();

        if (plannedHours == null || plannedHours <= 0) {
            throw new RentalValidationException(
                    "Для почасового тарифа должно быть указано количество часов"
            );
        }

        return scooter.getModel()
                .getPricePerHour()
                .multiply(BigDecimal.valueOf(plannedHours));
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(
                    name + " не задан"
            );
        }
        return obj;
    }
}