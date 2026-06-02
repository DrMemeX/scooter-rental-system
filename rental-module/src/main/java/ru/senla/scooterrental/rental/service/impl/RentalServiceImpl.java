package ru.senla.scooterrental.rental.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.ScooterService;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.ActiveRentalAlreadyExistsException;
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.repository.RentalRepository;
import ru.senla.scooterrental.rental.service.PricingService;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.rental.service.RentalCalculationService;
import ru.senla.scooterrental.rental.service.RentalTerminationResolver;
import ru.senla.scooterrental.rental.service.RentalValidator;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class RentalServiceImpl implements RentalService {

    private static final Logger log =
            LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalRepository rentalRepository;
    private final ScooterService scooterService;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final UserService userService;
    private final RentalCalculationService calculationService;
    private final RentalTerminationResolver terminationResolver;
    private final RentalValidator rentalValidator;

    public RentalServiceImpl(RentalRepository rentalRepository,
                             ScooterService scooterService,
                             PricingService pricingService,
                             DiscountService discountService,
                             UserService userService,
                             RentalCalculationService calculationService,
                             RentalTerminationResolver terminationResolver,
                             RentalValidator rentalValidator) {

        this.rentalRepository = rentalRepository;
        this.scooterService = scooterService;
        this.pricingService = pricingService;
        this.discountService = discountService;
        this.userService = userService;
        this.calculationService = calculationService;
        this.terminationResolver = terminationResolver;
        this.rentalValidator = rentalValidator;
    }

    @Override
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

        rentalValidator.validatePositiveId(userId, "ID пользователя");
        rentalValidator.validatePositiveId(scooterId, "ID самоката");
        rentalValidator.requireNonNull(tariffType, "Тип тарифа");

        User user = userService.getById(userId);
        Scooter scooter = scooterService.getScooterById(scooterId);

        rentalValidator.ensureUserCanStartRental(user);

        rentalRepository.findUnfinishedByUserId(userId)
                .ifPresent(rental -> {
                    throw new ActiveRentalAlreadyExistsException(
                            "У пользователя с ID " + userId
                                    + " уже есть незавершённая аренда"
                    );
                });

        rentalValidator.ensureCanPayForRental(
                user,
                scooter,
                tariffType,
                plannedHours
        );

        Integer maxAllowedMinutes =
                calculationService.calculateMaxAllowedMinutesForStart(
                        user,
                        scooter,
                        tariffType,
                        plannedHours
                );

        scooterService.rentScooter(scooterId);

        Rental rental = new Rental(user, scooter, tariffType, plannedHours);

        if (tariffType == TariffType.MINUTE || tariffType == TariffType.HOUR) {
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

    @Override
    public Rental startRental(Long userId,
                              Long scooterId,
                              TariffType tariffType) {
        return startRental(userId, scooterId, tariffType, null);
    }

    @Override
    public Rental finishRental(Long rentalId,
                               Long rentalPointId,
                               double distanceKm,
                               String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.USER_FINISHED,
                false
        );
    }

    @Override
    public Rental finishRental(Long rentalId,
                               Long rentalPointId,
                               String promoCode) {
        return finishRental(rentalId, rentalPointId, 0, promoCode);
    }

    @Override
    public Rental finishRental(Long rentalId, Long rentalPointId) {
        return finishRental(rentalId, rentalPointId, 0, null);
    }

    @Override
    public Rental finishDueToBatteryDepleted(Long rentalId,
                                             Long rentalPointId,
                                             double distanceKm,
                                             String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.BATTERY_DEPLETED,
                false
        );
    }

    @Override
    public Rental finishDueToBatteryDepleted(Long rentalId,
                                             Long rentalPointId,
                                             String promoCode) {
        return finishDueToBatteryDepleted(
                rentalId,
                rentalPointId,
                0,
                promoCode
        );
    }

    @Override
    public Rental finishDueToTechnicalBreakdown(Long rentalId,
                                                Long rentalPointId,
                                                double distanceKm,
                                                String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.TECHNICAL_BREAKDOWN,
                false
        );
    }

    @Override
    public Rental finishDueToTechnicalBreakdown(Long rentalId,
                                                Long rentalPointId,
                                                String promoCode) {
        return finishDueToTechnicalBreakdown(
                rentalId,
                rentalPointId,
                0,
                promoCode
        );
    }

    @Override
    public Rental finishDueToPaymentLimitExceeded(Long rentalId,
                                                  Long rentalPointId) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                0,
                null,
                TerminationReason.PAYMENT_LIMIT_EXCEEDED,
                false
        );
    }

    @Override
    public Rental finishDueToUserDamage(Long rentalId,
                                        Long rentalPointId,
                                        double distanceKm,
                                        String promoCode) {
        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.USER_DAMAGE,
                false
        );
    }

    @Override
    public Rental finishDueToUserDamage(Long rentalId,
                                        Long rentalPointId,
                                        String promoCode) {
        return finishDueToUserDamage(
                rentalId,
                rentalPointId,
                0,
                promoCode
        );
    }

    @Override
    public Rental requestManualFinish(Long rentalId) {
        log.info("Requesting manual finish: rentalId={}", rentalId);

        Rental rental = getRentalOrThrow(rentalId);

        rentalValidator.validateActiveRental(rental);

        scooterService.requestReturnVerification(rental.getScooterId());

        rental.requestManualFinish();

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Manual finish requested successfully: rentalId={}",
                savedRental.getId()
        );

        return savedRental;
    }

    @Override
    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId,
                                      double distanceKm,
                                      String promoCode) {

        return finishRentalInternal(
                rentalId,
                rentalPointId,
                distanceKm,
                promoCode,
                TerminationReason.MANAGER_CONFIRMED_RETURN,
                true
        );
    }

    @Override
    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId,
                                      String promoCode) {
        return approveManualFinish(rentalId, rentalPointId, 0, promoCode);
    }

    @Override
    public Rental approveManualFinish(Long rentalId, Long rentalPointId) {
        return approveManualFinish(rentalId, rentalPointId, 0, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Rental getRentalOrThrow(Long rentalId) {
        rentalValidator.validatePositiveId(rentalId, "ID аренды");

        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(
                        "Аренда с ID " + rentalId + " не найдена"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> getRentalsByUserId(Long userId) {
        rentalValidator.validatePositiveId(userId, "ID пользователя");

        return rentalRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> getRentalsByScooterId(Long scooterId) {
        rentalValidator.validatePositiveId(scooterId, "ID самоката");

        return rentalRepository.findByScooterId(scooterId);
    }

    private Rental finishRentalInternal(Long rentalId,
                                        Long rentalPointId,
                                        double distanceKm,
                                        String promoCode,
                                        TerminationReason requestedReason,
                                        boolean manualApproval) {

        log.info(
                "Finishing rental: rentalId={}, rentalPointId={}, reason={}, manualApproval={}",
                rentalId,
                rentalPointId,
                requestedReason,
                manualApproval
        );

        rentalValidator.validatePositiveId(rentalPointId, "ID точки проката");

        Rental rental = getRentalOrThrow(rentalId);

        if (manualApproval) {
            rentalValidator.validatePendingManualFinish(rental);
        } else {
            rentalValidator.validateActiveRental(rental);
        }

        Scooter scooter = scooterService.getScooterById(rental.getScooterId());

        long actualMinutes = calculationService.calculateActualMinutes(rental);

        long effectiveMinutes = calculationService.resolveEffectiveMinutes(
                rental,
                actualMinutes
        );

        TerminationReason reason =
                terminationResolver.resolveTerminationReason(
                        rental,
                        rental.getUser(),
                        scooter,
                        actualMinutes,
                        effectiveMinutes,
                        requestedReason
                );

        rentalValidator.validateRideDistance(
                scooter,
                distanceKm,
                effectiveMinutes
        );

        BigDecimal totalCost = pricingService.calculate(
                rental,
                scooter,
                reason,
                effectiveMinutes
        );

        rentalValidator.validatePromoCodeNotUsedByUser(
                rental.getUserId(),
                promoCode
        );

        BigDecimal finalCost = discountService.applyDiscount(
                totalCost,
                promoCode
        );

        updateScooterAfterRide(rental, scooter, rentalPointId, distanceKm);

        userService.subtractBalance(rental.getUserId(), finalCost);

        rental.recordDistance(distanceKm);

        if (manualApproval) {
            rental.approveManualFinish(finalCost, reason);
        } else {
            rental.finish(finalCost, reason);
        }

        Rental savedRental = rentalRepository.save(rental);

        log.info(
                "Rental finished successfully: rentalId={}, totalCost={}, reason={}, manualApproval={}",
                savedRental.getId(),
                finalCost,
                reason,
                manualApproval
        );

        return savedRental;
    }

    private void updateScooterAfterRide(Rental rental,
                                        Scooter scooter,
                                        Long rentalPointId,
                                        double distanceKm) {
        if (distanceKm > 0) {
            double chargeConsumption =
                    calculationService.calculateChargeConsumption(
                            scooter,
                            distanceKm
                    );

            scooterService.addMileage(
                    rental.getScooterId(),
                    distanceKm
            );

            scooterService.consumeCharge(
                    rental.getScooterId(),
                    chargeConsumption
            );
        }

        scooterService.returnScooter(
                rental.getScooterId(),
                rentalPointId
        );
    }
}