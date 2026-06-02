package ru.senla.scooterrental.rental.service.impl.resolver;

import org.springframework.stereotype.Service;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.service.RentalTerminationResolver;
import ru.senla.scooterrental.rental.service.RentalCalculationService;
import ru.senla.scooterrental.user.entity.User;

@Service
public class RentalTerminationResolverImpl implements RentalTerminationResolver {

    private final RentalCalculationService calculationService;

    public RentalTerminationResolverImpl(RentalCalculationService calculationService) {
        this.calculationService = requireNonNull(
                calculationService,
                "Сервис расчётов аренды"
        );
    }

    @Override
    public TerminationReason resolveTerminationReason(Rental rental,
                                                      User user,
                                                      Scooter scooter,
                                                      long actualMinutes,
                                                      long effectiveMinutes,
                                                      TerminationReason requestedReason) {
        requireNonNull(rental, "Аренда");
        requireNonNull(user, "Пользователь");
        requireNonNull(scooter, "Самокат");
        requireNonNull(requestedReason, "Причина завершения аренды");

        if (rental.getTariffType() == TariffType.SUBSCRIPTION) {
            return requestedReason;
        }

        Integer maxAllowedMinutes = rental.getMaxAllowedMinutes();

        if (maxAllowedMinutes == null || maxAllowedMinutes <= 0) {
            throw new RentalValidationException(
                    "Для тарифа с оплатой по времени не задан лимит"
            );
        }

        if (actualMinutes <= effectiveMinutes) {
            return requestedReason;
        }

        LimitReason limitReason = resolveLimitReason(
                rental,
                user,
                scooter
        );

        if (limitReason == LimitReason.PAYMENT) {
            return TerminationReason.PAYMENT_LIMIT_EXCEEDED;
        }

        if (limitReason == LimitReason.BATTERY) {
            return TerminationReason.BATTERY_DEPLETED;
        }

        return requestedReason;
    }

    private LimitReason resolveLimitReason(Rental rental,
                                           User user,
                                           Scooter scooter) {
        if (rental.getTariffType() == TariffType.SUBSCRIPTION) {
            return LimitReason.NONE;
        }

        int maxAllowedMinutes = rental.getMaxAllowedMinutes();

        int maxAllowedMinutesByBattery =
                calculationService.calculateAvailableMinutesByBattery(scooter);

        int maxAllowedMinutesByMoney;

        if (rental.getTariffType() == TariffType.MINUTE) {
            maxAllowedMinutesByMoney =
                    calculationService.calculateMaxAllowedMinutes(user, scooter);
        } else {
            maxAllowedMinutesByMoney =
                    calculationService.calculateAvailableMinutesByMoneyForHour(
                            user,
                            scooter
                    );
        }

        if (maxAllowedMinutes == maxAllowedMinutesByMoney
                && maxAllowedMinutes <= maxAllowedMinutesByBattery) {
            return LimitReason.PAYMENT;
        }

        if (maxAllowedMinutes == maxAllowedMinutesByBattery
                && maxAllowedMinutes < maxAllowedMinutesByMoney) {
            return LimitReason.BATTERY;
        }

        return LimitReason.NONE;
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(name + " не задан");
        }

        return obj;
    }

    private enum LimitReason {
        NONE,
        PAYMENT,
        BATTERY
    }
}