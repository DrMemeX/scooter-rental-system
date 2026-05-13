package ru.senla.scooterrental.rental.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {

    private static final Logger log =
            LoggerFactory.getLogger(PricingService.class);

    public BigDecimal calculate(Rental rental, Scooter scooter) {
        log.info("Starting rental price calculation");

        requireNonNull(rental, "Аренда");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");
        requireNonNull(rental.getTariffType(), "Тип тарифа");

        long minutes = calculateRentalMinutes(rental.getStartTime());

        BigDecimal price = switch (rental.getTariffType()) {
            case MINUTE -> calculateMinutePrice(minutes, rental, scooter);
            case HOUR -> calculateHourPrice(rental, scooter);
            case SUBSCRIPTION -> BigDecimal.ZERO;
        };

        log.info(
                "Rental price calculated successfully: rentalId={}, scooterId={}, tariffType={}, price={}",
                rental.getId(),
                scooter.getId(),
                rental.getTariffType(),
                price
        );

        return price;
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