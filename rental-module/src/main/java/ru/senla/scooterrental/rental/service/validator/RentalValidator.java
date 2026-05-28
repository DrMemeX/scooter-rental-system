package ru.senla.scooterrental.rental.service.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;
import ru.senla.scooterrental.rental.service.calculator.RentalCalculationService;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;

import java.math.BigDecimal;

@Service
public class RentalValidator {

    private static final Logger log =
            LoggerFactory.getLogger(RentalValidator.class);

    private final RentalRepository rentalRepository;
    private final RentalCalculationService calculationService;

    public RentalValidator(RentalRepository rentalRepository,
                           RentalCalculationService calculationService) {
        this.rentalRepository = requireNonNull(
                rentalRepository,
                "Репозиторий аренд"
        );
        this.calculationService = requireNonNull(
                calculationService,
                "Сервис расчётов аренды"
        );
    }

    public void validatePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new RentalValidationException(
                    name + " должен быть положительным"
            );
        }
    }

    public void ensureUserCanStartRental(User user) {
        requireNonNull(user, "Пользователь");

        if (user.isBlocked()) {
            throw new RentalValidationException(
                    "Заблокированный пользователь не может начать аренду"
            );
        }
    }

    public void ensureCanPayForRental(User user,
                                      Scooter scooter,
                                      TariffType tariffType,
                                      Integer plannedHours) {
        requireNonNull(user, "Пользователь");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");
        requireNonNull(tariffType, "Тип тарифа");

        if (tariffType == TariffType.MINUTE) {
            validateMinuteTariffStart(user, scooter);
            return;
        }

        if (tariffType == TariffType.HOUR) {
            validateHourTariffStart(user, scooter, plannedHours);
            return;
        }

        if (tariffType == TariffType.SUBSCRIPTION
                && !user.hasActiveSubscription()) {
            throw new RentalValidationException(
                    "У пользователя нет активного абонемента"
            );
        }
    }

    public void validatePromoCodeNotUsedByUser(Long userId, String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return;
        }

        boolean alreadyUsed = rentalRepository.existsByUserIdAndPromoCodeCode(
                userId,
                promoCode
        );

        if (alreadyUsed) {
            throw new RentalValidationException(
                    "Пользователь уже использовал данный промокод"
            );
        }
    }

    public void validateRideDistance(Scooter scooter,
                                     double distanceKm,
                                     long actualMinutes) {
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        if (distanceKm < 0) {
            throw new RentalValidationException(
                    "Дистанция поездки не может быть отрицательной"
            );
        }

        if (distanceKm == 0) {
            return;
        }

        double hours = actualMinutes / 60.0;

        double maxDistanceBySpeed =
                scooter.getModel().getMaxSpeedKmPerHour() * hours;

        double maxDistanceByCharge =
                scooter.getCurrentCharge()
                        / scooter.getModel().getConsumptionPerKm();

        double allowedDistance = Math.min(
                maxDistanceBySpeed,
                maxDistanceByCharge
        );

        if (distanceKm > allowedDistance) {
            throw new RentalValidationException(
                    "Указанная дистанция невозможна для данной длительности аренды и текущего заряда"
            );
        }
    }

    public void validateActiveRental(Rental rental) {
        requireNonNull(rental, "Аренда");

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RentalValidationException(
                    "Операция доступна только для активной аренды"
            );
        }
    }

    public void validatePendingManualFinish(Rental rental) {
        requireNonNull(rental, "Аренда");

        if (rental.getStatus() != RentalStatus.PENDING_MANAGER_CONFIRMATION) {
            throw new RentalValidationException(
                    "Подтвердить можно только аренду, ожидающую проверки менеджером"
            );
        }
    }

    public <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(name + " не задан");
        }

        return obj;
    }

    private void validateMinuteTariffStart(User user, Scooter scooter) {
        BigDecimal pricePerMinute = scooter.getModel().getPricePerMinute();

        if (user.getBalance().compareTo(pricePerMinute) < 0) {
            throw new RentalValidationException(
                    "Недостаточно средств для начала поминутной аренды"
            );
        }
    }

    private void validateHourTariffStart(User user,
                                         Scooter scooter,
                                         Integer plannedHours) {
        if (plannedHours == null || plannedHours <= 0) {
            throw new RentalValidationException(
                    "Для почасовой аренды должно быть указано количество часов"
            );
        }

        int plannedMinutes = plannedHours * 60;

        int maxAllowedMinutesByBattery =
                calculationService.calculateAvailableMinutesByBattery(scooter);

        int maxAllowedMinutesByMoney =
                calculationService.calculateAvailableMinutesByMoneyForHour(
                        user,
                        scooter
                );

        int billableMinutes = Math.min(
                plannedMinutes,
                Math.min(
                        maxAllowedMinutesByBattery,
                        maxAllowedMinutesByMoney
                )
        );

        log.info(
                "Rental duration adjusted: requested={} min, byBattery={} min, byMoney={} min, result={} min",
                plannedMinutes,
                maxAllowedMinutesByBattery,
                maxAllowedMinutesByMoney,
                billableMinutes
        );

        if (billableMinutes <= 0) {
            throw new RentalValidationException(
                    "Недостаточно заряда или средств для начала почасовой аренды"
            );
        }
    }
}