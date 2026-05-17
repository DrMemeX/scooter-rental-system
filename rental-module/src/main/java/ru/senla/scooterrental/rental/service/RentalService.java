package ru.senla.scooterrental.rental.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.ActiveRentalAlreadyExistsException;
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RentalService {

    private static final Logger log =
            LoggerFactory.getLogger(RentalService.class);

    private final RentalRepository rentalRepository;
    private final FleetService fleetService;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final UserService userService;

    public RentalService(RentalRepository rentalRepository,
                         FleetService fleetService,
                         PricingService pricingService,
                         DiscountService discountService,
                         UserService userService) {

        this.rentalRepository = requireNonNull(rentalRepository, "Репозиторий аренд");
        this.fleetService = requireNonNull(fleetService, "Сервис парка самокатов");
        this.pricingService = requireNonNull(pricingService, "Сервис расчёта стоимости");
        this.discountService = requireNonNull(discountService, "Сервис скидок");
        this.userService = requireNonNull(userService, "Сервис пользователей");
    }

    public Rental startRental(Long userId,
                              Long scooterId,
                              TariffType tariffType,
                              Integer plannedHours) {

        log.info(
                "Starting rental: userId={}, scooterId={}, tariffType={}",
                userId,
                scooterId,
                tariffType
        );

        validatePositiveId(userId, "ID пользователя");
        validatePositiveId(scooterId, "ID самоката");
        requireNonNull(tariffType, "Тип тарифа");

        User user = userService.getById(userId);
        Scooter scooter = fleetService.getScooterById(scooterId);

        if (user.isBlocked()) {
            throw new RentalValidationException(
                    "Заблокированный пользователь не может начать аренду"
            );
        }

        rentalRepository.findUnfinishedByUserId(userId)
                .ifPresent(rental -> {
                    throw new ActiveRentalAlreadyExistsException(
                            "У пользователя с ID " + userId + " уже есть незавершённая аренда"
                    );
                });

        ensureCanPayForRental(user, scooter, tariffType, plannedHours);

        Integer maxAllowedMinutes = null;

        if (tariffType == TariffType.MINUTE) {
            maxAllowedMinutes = calculateMaxAllowedMinutes(user, scooter);
        }

        fleetService.rentScooter(scooterId);

        Rental rental = new Rental(user, scooter, tariffType, plannedHours);

        if (tariffType == TariffType.MINUTE) {
            rental.setMaxAllowedMinutes(maxAllowedMinutes);
        }

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Rental started successfully: rentalId={}, userId={}, scooterId={}",
                savedRental.getId(),
                userId,
                scooterId
        );

        return savedRental;
    }

    public Rental startRental(Long userId,
                              Long scooterId,
                              TariffType tariffType) {
        return startRental(userId, scooterId, tariffType, null);
    }

    public Rental finishRental(Long rentalId,
                               Long rentalPointId,
                               double distanceKm,
                               String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.USER_FINISHED
        );
    }

    public Rental finishRental(Long rentalId,
                               Long rentalPointId,
                               String promoCode) {
        return finishRental(rentalId, rentalPointId, 0, promoCode);
    }

    public Rental finishRental(Long rentalId, Long rentalPointId) {
        return finishRental(rentalId, rentalPointId, 0, null);
    }

    public Rental finishDueToBatteryDepleted(Long rentalId,
                                             Long rentalPointId,
                                             double distanceKm,
                                             String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.BATTERY_DEPLETED
        );
    }

    public Rental finishDueToBatteryDepleted(Long rentalId,
                                             Long rentalPointId,
                                             String promoCode) {
        return finishDueToBatteryDepleted(rentalId, rentalPointId, 0, promoCode);
    }

    public Rental finishDueToTechnicalBreakdown(Long rentalId,
                                                Long rentalPointId,
                                                double distanceKm,
                                                String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.TECHNICAL_BREAKDOWN
        );
    }

    public Rental finishDueToTechnicalBreakdown(Long rentalId,
                                                Long rentalPointId,
                                                String promoCode) {
        return finishDueToTechnicalBreakdown(rentalId, rentalPointId, 0, promoCode);
    }

    public Rental finishDueToUserDamage(Long rentalId,
                                        Long rentalPointId,
                                        double distanceKm,
                                        String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.USER_DAMAGE
        );
    }

    public Rental finishDueToUserDamage(Long rentalId,
                                        Long rentalPointId,
                                        String promoCode) {
        return finishDueToUserDamage(rentalId, rentalPointId, 0, promoCode);
    }

    private Rental finishRentalInternal(Long rentalId,
                                        Long rentalPointId,
                                        double distanceKm,
                                        String promoCode,
                                        TerminationReason reason) {

        log.info(
                "Finishing rental: rentalId={}, rentalPointId={}, reason={}",
                rentalId,
                rentalPointId,
                reason
        );
        validatePositiveId(rentalPointId, "ID точки проката");

        Rental rental = getRentalOrThrow(rentalId);
        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        long actualMinutes = calculateActualMinutes(rental);

        validateMinuteRentalTimeLimit(rental, actualMinutes);
        validateRideDistance(scooter, distanceKm, actualMinutes);

        BigDecimal totalCost = pricingService.calculate(rental, scooter, reason);

        validatePromoCodeNotUsedByUser(rental.getUserId(), promoCode);

        BigDecimal finalCost = discountService.applyDiscount(totalCost, promoCode);

        if (distanceKm > 0) {
            double chargeConsumption = calculateChargeConsumption(scooter, distanceKm);

            fleetService.addMileage(rental.getScooterId(), distanceKm);
            fleetService.consumeCharge(rental.getScooterId(), chargeConsumption);
        }

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        userService.subtractBalance(rental.getUserId(), finalCost);

        rental.recordDistance(distanceKm);
        rental.finish(finalCost, reason);

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Rental finished successfully: rentalId={}, totalCost={}, reason={}",
                savedRental.getId(),
                finalCost,
                reason
        );

        return savedRental;
    }

    public Rental requestManualFinish(Long rentalId) {
        log.info("Requesting manual finish: rentalId={}", rentalId);

        Rental rental = getRentalOrThrow(rentalId);

        fleetService.requestReturnVerification(rental.getScooterId());

        rental.requestManualFinish();

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Manual finish requested successfully: rentalId={}",
                savedRental.getId()
        );

        return savedRental;
    }

    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId,
                                      double distanceKm,
                                      String promoCode) {

        log.info(
                "Approving manual finish: rentalId={}, rentalPointId={}",
                rentalId,
                rentalPointId
        );
        validatePositiveId(rentalPointId, "ID точки проката");

        Rental rental = getRentalOrThrow(rentalId);
        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        long actualMinutes = calculateActualMinutes(rental);

        validateMinuteRentalTimeLimit(rental, actualMinutes);
        validateRideDistance(scooter, distanceKm, actualMinutes);

        BigDecimal totalCost = pricingService.calculate(rental, scooter);

        validatePromoCodeNotUsedByUser(rental.getUserId(), promoCode);

        BigDecimal finalCost = discountService.applyDiscount(totalCost, promoCode);

        if (distanceKm > 0) {
            double chargeConsumption = calculateChargeConsumption(scooter, distanceKm);

            fleetService.addMileage(rental.getScooterId(), distanceKm);
            fleetService.consumeCharge(rental.getScooterId(), chargeConsumption);
        }

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        userService.subtractBalance(rental.getUserId(), finalCost);

        rental.recordDistance(distanceKm);
        rental.approveManualFinish(
                finalCost,
                TerminationReason.MANAGER_CONFIRMED_RETURN
        );

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Manual finish approved successfully: rentalId={}, totalCost={}",
                savedRental.getId(),
                finalCost
        );

        return savedRental;
    }

    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId,
                                      String promoCode) {
        return approveManualFinish(rentalId, rentalPointId, 0, promoCode);
    }

    public Rental approveManualFinish(Long rentalId, Long rentalPointId) {
        return approveManualFinish(rentalId, rentalPointId, 0, null);
    }

    @Transactional(readOnly = true)
    public Rental getRentalOrThrow(Long rentalId) {
        validatePositiveId(rentalId, "ID аренды");

        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(
                        "Аренда с ID " + rentalId + " не найдена"
                ));
    }

    @Transactional(readOnly = true)
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Rental> getRentalsByUserId(Long userId) {
        validatePositiveId(userId, "ID пользователя");

        return rentalRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Rental> getRentalsByScooterId(Long scooterId) {
        validatePositiveId(scooterId, "ID самоката");

        return rentalRepository.findByScooterId(scooterId);
    }

    private void ensureCanPayForRental(User user,
                                       Scooter scooter,
                                       TariffType tariffType,
                                       Integer plannedHours) {
        requireNonNull(user, "Пользователь");
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");
        requireNonNull(tariffType, "Тип тарифа");

        if (tariffType == TariffType.MINUTE) {
            BigDecimal pricePerMinute = scooter.getModel().getPricePerMinute();

            if (user.getBalance().compareTo(pricePerMinute) < 0) {
                throw new RentalValidationException(
                        "Недостаточно средств для начала поминутной аренды"
                );
            }

            return;
        }

        if (tariffType == TariffType.HOUR) {
            if (plannedHours == null || plannedHours <= 0) {
                throw new RentalValidationException(
                        "Для почасовой аренды должно быть указано количество часов"
                );
            }

            BigDecimal requiredAmount = scooter.getModel()
                    .getPricePerHour()
                    .multiply(BigDecimal.valueOf(plannedHours));

            if (user.getBalance().compareTo(requiredAmount) < 0) {
                throw new RentalValidationException(
                        "Недостаточно средств для выбранного почасового тарифа"
                );
            }

            return;
        }

        if (tariffType == TariffType.SUBSCRIPTION) {
            if (!user.hasActiveSubscription()) {
                throw new RentalValidationException(
                        "У пользователя нет активного абонемента"
                );
            }

            return;
        }
    }

    private void validatePromoCodeNotUsedByUser(Long userId, String promoCode) {
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

    private void validateMinuteRentalTimeLimit(Rental rental, long actualMinutes) {
        if (rental.getTariffType() != TariffType.MINUTE) {
            return;
        }

        Integer maxAllowedMinutes = rental.getMaxAllowedMinutes();

        if (maxAllowedMinutes == null || maxAllowedMinutes <= 0) {
            throw new RentalValidationException(
                    "Для поминутного тарифа не задан лимит оплаченного времени"
            );
        }

        if (actualMinutes > maxAllowedMinutes) {
            throw new RentalValidationException(
                    "Время поездки превышает оплачиваемый лимит пользователя"
            );
        }
    }

    private void validateRideDistance(Scooter scooter,
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

        double maxDistanceBySpeed = scooter.getModel().getMaxSpeedKmPerHour() * hours;
        double maxDistanceByCharge = scooter.getCurrentCharge()
                / scooter.getModel().getConsumptionPerKm();

        double allowedDistance = Math.min(maxDistanceBySpeed, maxDistanceByCharge);

        if (distanceKm > allowedDistance) {
            throw new RentalValidationException(
                    "Указанная дистанция невозможна для данной длительности аренды и текущего заряда"
            );
        }
    }

    private long calculateActualMinutes(Rental rental) {
        requireNonNull(rental, "Аренда");
        requireNonNull(rental.getStartTime(), "Время начала аренды");

        long minutes = Duration.between(
                rental.getStartTime(),
                LocalDateTime.now()
        ).toMinutes();

        return Math.max(minutes, 1);
    }

    private double calculateChargeConsumption(Scooter scooter, double distanceKm) {
        requireNonNull(scooter, "Самокат");
        requireNonNull(scooter.getModel(), "Модель самоката");

        return Math.ceil(distanceKm * scooter.getModel().getConsumptionPerKm());
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(name + " не задан");
        }

        return obj;
    }

    private void validatePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new RentalValidationException(name + " должен быть положительным");
        }
    }

    private Integer calculateMaxAllowedMinutes(User user, Scooter scooter) {
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
}