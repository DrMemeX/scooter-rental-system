package ru.senla.scooterrental.rental.service;

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
import java.util.List;

public class RentalService {

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

        validatePositiveId(userId, "ID пользователя");
        validatePositiveId(scooterId, "ID самоката");
        requireNonNull(tariffType, "Тип тарифа");

        User user = userService.getById(userId);
        Scooter scooter = fleetService.getScooterById(scooterId);

        rentalRepository.findUnfinishedByUserId(userId)
                .ifPresent(rental -> {
                    throw new ActiveRentalAlreadyExistsException(
                            "У пользователя с ID " + userId + " уже есть незавершённая аренда"
                    );
                });

        Integer maxAllowedMinutes = null;

        if (tariffType == TariffType.MINUTE) {
            maxAllowedMinutes = calculateMaxAllowedMinutes(user, scooter);
        }

        fleetService.rentScooter(scooterId);

        Rental rental = new Rental(userId, scooterId, tariffType, plannedHours);

        if (tariffType == TariffType.MINUTE) {
            rental.setMaxAllowedMinutes(maxAllowedMinutes);
        }

        return rentalRepository.save(rental);
    }

    public Rental startRental(Long userId,
                              Long scooterId,
                              TariffType tariffType) {
        return startRental(userId, scooterId, tariffType, null);
    }

    public Rental finishRental(Long rentalId,
                               Long rentalPointId,
                               String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                promoCode,
                TerminationReason.USER_FINISHED
        );
    }

    public Rental finishDueToBatteryDepleted(Long rentalId,
                                             Long rentalPointId,
                                             String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                promoCode,
                TerminationReason.BATTERY_DEPLETED
        );
    }

    public Rental finishDueToTechnicalBreakdown(Long rentalId,
                                                Long rentalPointId,
                                                String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                promoCode,
                TerminationReason.TECHNICAL_BREAKDOWN
        );
    }

    public Rental finishDueToUserDamage(Long rentalId,
                                        Long rentalPointId,
                                        String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                promoCode,
                TerminationReason.USER_DAMAGE
        );
    }

    private Rental finishRentalInternal(Long rentalId,
                                        Long rentalPointId,
                                        String promoCode,
                                        TerminationReason reason) {

        Rental rental = getRentalOrThrow(rentalId);
        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        BigDecimal totalCost = pricingService.calculate(rental, scooter);
        BigDecimal finalCost = discountService.applyDiscount(totalCost, promoCode);

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        userService.subtractBalance(rental.getUserId(), finalCost);

        rental.finish(finalCost, reason);

        return rentalRepository.save(rental);
    }

    public Rental finishRental(Long rentalId, Long rentalPointId) {
        return finishRental(rentalId, rentalPointId, null);
    }

    public Rental requestManualFinish(Long rentalId) {
        Rental rental = getRentalOrThrow(rentalId);

        fleetService.requestReturnVerification(rental.getScooterId());

        rental.requestManualFinish();

        return rentalRepository.save(rental);
    }

    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId,
                                      String promoCode) {

        Rental rental = getRentalOrThrow(rentalId);
        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        BigDecimal totalCost = pricingService.calculate(rental, scooter);
        BigDecimal finalCost = discountService.applyDiscount(totalCost, promoCode);

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        userService.subtractBalance(rental.getUserId(), finalCost);

        rental.approveManualFinish(
                finalCost,
                TerminationReason.MANAGER_CONFIRMED_RETURN
        );

        return rentalRepository.save(rental);
    }

    public Rental approveManualFinish(Long rentalId, Long rentalPointId) {
        return approveManualFinish(rentalId, rentalPointId, null);
    }

    public Rental getRentalOrThrow(Long rentalId) {
        validatePositiveId(rentalId, "ID аренды");

        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(
                        "Аренда с ID " + rentalId + " не найдена"
                ));
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> getRentalsByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public List<Rental> getRentalsByScooterId(Long scooterId) {
        return rentalRepository.findByScooterId(scooterId);
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