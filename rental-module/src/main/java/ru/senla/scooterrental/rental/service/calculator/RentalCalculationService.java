package ru.senla.scooterrental.rental.service.calculator;

import org.springframework.stereotype.Service;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RentalCalculationService {

    public long calculateActualMinutes(Rental rental) {
        requireNonNull(rental, "Аренда");
        requireNonNull(rental.getStartTime(), "Время начала аренды");

        long minutes = Duration.between(
                rental.getStartTime(),
                LocalDateTime.now()
        ).toMinutes();

        return Math.max(minutes, 1);
    }

    public long resolveEffectiveMinutes(Rental rental, long actualMinutes) {
        requireNonNull(rental, "Аренда");

        if (rental.getTariffType() == TariffType.SUBSCRIPTION) {
            return actualMinutes;
        }

        Integer maxAllowedMinutes = rental.getMaxAllowedMinutes();

        if (maxAllowedMinutes == null || maxAllowedMinutes <= 0) {
            throw new RentalValidationException(
                    "Для тарифа с оплатой по времени не задан лимит"
            );
        }

        return Math.min(actualMinutes, maxAllowedMinutes);
    }

    public double calculateChargeConsumption(Scooter scooter, double distanceKm) {
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        return Math.ceil(distanceKm * scooter.getModel().getConsumptionPerKm());
    }

    public int calculateMaxAllowedMinutes(User user, Scooter scooter) {
        requireNonNull(user, "Пользователь");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        BigDecimal pricePerMinute = scooter.getModel().getPricePerMinute();

        if (pricePerMinute.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RentalValidationException(
                    "Цена за минуту должна быть положительной"
            );
        }

        int maxAllowedMinutes = user.getBalance()
                .divideToIntegralValue(pricePerMinute)
                .intValue();

        if (maxAllowedMinutes <= 0) {
            throw new RentalValidationException(
                    "Недостаточно средств для начала поминутной аренды"
            );
        }

        return maxAllowedMinutes;
    }

    public int calculateAvailableMinutesByBattery(Scooter scooter) {
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        double maxDistanceByCharge =
                scooter.getCurrentCharge()
                        / scooter.getModel().getConsumptionPerKm();

        double maxHoursByCharge =
                maxDistanceByCharge
                        / scooter.getModel().getMaxSpeedKmPerHour();

        return (int) Math.floor(maxHoursByCharge * 60);
    }

    public int calculateAvailableMinutesByMoneyForHour(User user,
                                                       Scooter scooter) {
        requireNonNull(user, "Пользователь");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        BigDecimal pricePerMinute =
                scooter.getModel()
                        .getPricePerHour()
                        .divide(
                                BigDecimal.valueOf(60),
                                2,
                                RoundingMode.HALF_UP
                        );

        return user.getBalance()
                .divideToIntegralValue(pricePerMinute)
                .intValue();
    }

    public Integer calculateMaxAllowedMinutesForStart(User user,
                                                      Scooter scooter,
                                                      TariffType tariffType,
                                                      Integer plannedHours) {
        requireNonNull(tariffType, "Тип тарифа");

        if (tariffType == TariffType.MINUTE) {
            int byMoney = calculateMaxAllowedMinutes(user, scooter);
            int byBattery = calculateAvailableMinutesByBattery(scooter);

            return Math.min(byMoney, byBattery);
        }

        if (tariffType == TariffType.HOUR) {
            int plannedMinutes = plannedHours * 60;
            int byBattery = calculateAvailableMinutesByBattery(scooter);
            int byMoney = calculateAvailableMinutesByMoneyForHour(user, scooter);

            return Math.min(
                    plannedMinutes,
                    Math.min(byBattery, byMoney)
            );
        }

        return null;
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(name + " не задан");
        }

        return obj;
    }
}