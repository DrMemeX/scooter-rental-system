package ru.senla.scooterrental.rental.service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;
import ru.senla.scooterrental.rental.service.calculator.RentalCalculationService;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RentalValidatorTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalCalculationService calculationService;

    private RentalValidator validator;

    private User user;
    private Scooter scooter;
    private ScooterModel scooterModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validator = new RentalValidator(
                rentalRepository,
                calculationService
        );

        user = mock(User.class);
        scooter = mock(Scooter.class);
        scooterModel = mock(ScooterModel.class);

        when(scooter.getModel()).thenReturn(scooterModel);
    }

    // ID validation tests

    @Test
    void validatePositiveId_shouldPass_whenIdIsPositive() {
        assertDoesNotThrow(
                () -> validator.validatePositiveId(1L, "ID")
        );
    }

    @Test
    void validatePositiveId_shouldThrowException_whenIdIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePositiveId(null, "ID")
        );
    }

    @Test
    void validatePositiveId_shouldThrowException_whenIdIsZero() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePositiveId(0L, "ID")
        );
    }

    @Test
    void validatePositiveId_shouldThrowException_whenIdIsNegative() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePositiveId(-1L, "ID")
        );
    }

    // User validation tests

    @Test
    void ensureUserCanStartRental_shouldPass_whenUserIsActive() {
        when(user.isBlocked()).thenReturn(false);

        assertDoesNotThrow(
                () -> validator.ensureUserCanStartRental(user)
        );
    }

    @Test
    void ensureUserCanStartRental_shouldThrowException_whenUserIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureUserCanStartRental(null)
        );
    }

    @Test
    void ensureUserCanStartRental_shouldThrowException_whenUserIsBlocked() {
        when(user.isBlocked()).thenReturn(true);

        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureUserCanStartRental(user)
        );
    }

    // Start payment validation tests

    @Test
    void ensureCanPayForRental_shouldPass_forMinuteTariff() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(100));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        assertDoesNotThrow(
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.MINUTE,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forMinuteTariff_whenBalanceIsInsufficient() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(5));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.MINUTE,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldPass_forHourTariff() {
        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(120);

        when(calculationService.calculateAvailableMinutesByMoneyForHour(user, scooter))
                .thenReturn(120);

        assertDoesNotThrow(
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.HOUR,
                        2
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forHourTariff_whenPlannedHoursIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.HOUR,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forHourTariff_whenPlannedHoursIsZero() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.HOUR,
                        0
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forHourTariff_whenPlannedHoursIsNegative() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.HOUR,
                        -1
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forHourTariff_whenBatteryAndMoneyUnavailable() {
        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(0);

        when(calculationService.calculateAvailableMinutesByMoneyForHour(user, scooter))
                .thenReturn(0);

        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.HOUR,
                        2
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldPass_forSubscriptionTariff_whenUserHasSubscription() {
        when(user.hasActiveSubscription()).thenReturn(true);

        assertDoesNotThrow(
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.SUBSCRIPTION,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_forSubscriptionTariff_whenUserHasNoSubscription() {
        when(user.hasActiveSubscription()).thenReturn(false);

        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.SUBSCRIPTION,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_whenUserIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        null,
                        scooter,
                        TariffType.MINUTE,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        null,
                        TariffType.MINUTE,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        TariffType.MINUTE,
                        null
                )
        );
    }

    @Test
    void ensureCanPayForRental_shouldThrowException_whenTariffTypeIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.ensureCanPayForRental(
                        user,
                        scooter,
                        null,
                        null
                )
        );
    }

    // Promo code validation tests

    @Test
    void validatePromoCodeNotUsedByUser_shouldPass_whenPromoCodeIsNull() {
        assertDoesNotThrow(
                () -> validator.validatePromoCodeNotUsedByUser(1L, null)
        );

        verify(rentalRepository, never())
                .existsByUserIdAndPromoCodeCode(1L, null);
    }

    @Test
    void validatePromoCodeNotUsedByUser_shouldPass_whenPromoCodeIsBlank() {
        assertDoesNotThrow(
                () -> validator.validatePromoCodeNotUsedByUser(1L, "   ")
        );

        verify(rentalRepository, never())
                .existsByUserIdAndPromoCodeCode(1L, "   ");
    }

    @Test
    void validatePromoCodeNotUsedByUser_shouldPass_whenPromoCodeWasNotUsed() {
        when(rentalRepository.existsByUserIdAndPromoCodeCode(1L, "SALE10"))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> validator.validatePromoCodeNotUsedByUser(1L, "SALE10")
        );

        verify(rentalRepository)
                .existsByUserIdAndPromoCodeCode(1L, "SALE10");
    }

    @Test
    void validatePromoCodeNotUsedByUser_shouldThrowException_whenPromoCodeWasAlreadyUsed() {
        when(rentalRepository.existsByUserIdAndPromoCodeCode(1L, "SALE10"))
                .thenReturn(true);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePromoCodeNotUsedByUser(1L, "SALE10")
        );
    }

    // Ride distance validation tests

    @Test
    void validateRideDistance_shouldPass_whenDistanceIsZero() {
        assertDoesNotThrow(
                () -> validator.validateRideDistance(scooter, 0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldPass_whenDistanceIsPossible() {
        when(scooter.getCurrentCharge()).thenReturn(100.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(20.0);
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);

        assertDoesNotThrow(
                () -> validator.validateRideDistance(scooter, 10.0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validateRideDistance(null, 10.0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validateRideDistance(scooter, 10.0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldThrowException_whenDistanceIsNegative() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validateRideDistance(scooter, -1.0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldThrowException_whenDistanceExceedsSpeedLimit() {
        when(scooter.getCurrentCharge()).thenReturn(1000.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(20.0);
        when(scooterModel.getConsumptionPerKm()).thenReturn(1.0);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validateRideDistance(scooter, 30.0, 60)
        );
    }

    @Test
    void validateRideDistance_shouldThrowException_whenDistanceExceedsChargeLimit() {
        when(scooter.getCurrentCharge()).thenReturn(10.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(100.0);
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validateRideDistance(scooter, 10.0, 60)
        );
    }

    // Rental status validation tests

    @Test
    void validateActiveRental_shouldPass_whenRentalIsActive() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus()).thenReturn(RentalStatus.ACTIVE);

        assertDoesNotThrow(
                () -> validator.validateActiveRental(rental)
        );
    }

    @Test
    void validateActiveRental_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validateActiveRental(null)
        );
    }

    @Test
    void validateActiveRental_shouldThrowException_whenRentalIsFinished() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus()).thenReturn(RentalStatus.FINISHED);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validateActiveRental(rental)
        );
    }

    @Test
    void validateActiveRental_shouldThrowException_whenRentalIsPendingManualFinish() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus())
                .thenReturn(RentalStatus.PENDING_MANAGER_CONFIRMATION);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validateActiveRental(rental)
        );
    }

    @Test
    void validatePendingManualFinish_shouldPass_whenRentalIsPendingManualFinish() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus())
                .thenReturn(RentalStatus.PENDING_MANAGER_CONFIRMATION);

        assertDoesNotThrow(
                () -> validator.validatePendingManualFinish(rental)
        );
    }

    @Test
    void validatePendingManualFinish_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePendingManualFinish(null)
        );
    }

    @Test
    void validatePendingManualFinish_shouldThrowException_whenRentalIsActive() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus()).thenReturn(RentalStatus.ACTIVE);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePendingManualFinish(rental)
        );
    }

    @Test
    void validatePendingManualFinish_shouldThrowException_whenRentalIsFinished() {
        Rental rental = mock(Rental.class);

        when(rental.getStatus()).thenReturn(RentalStatus.FINISHED);

        assertThrows(
                RentalValidationException.class,
                () -> validator.validatePendingManualFinish(rental)
        );
    }

    // Common null validation tests

    @Test
    void requireNonNull_shouldReturnObject_whenObjectIsNotNull() {
        String result = validator.requireNonNull("value", "Значение");

        assertEquals("value", result);
    }

    @Test
    void requireNonNull_shouldThrowException_whenObjectIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> validator.requireNonNull(null, "Значение")
        );
    }
}