package ru.senla.scooterrental.rental.service.resolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.service.RentalCalculationService;
import ru.senla.scooterrental.rental.service.impl.resolver.RentalTerminationResolverImpl;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RentalTerminationResolverTest {

    private RentalCalculationService calculationService;
    private RentalTerminationResolverImpl resolver;

    private Rental rental;
    private User user;
    private Scooter scooter;

    @BeforeEach
    void setUp() {
        calculationService = mock(RentalCalculationService.class);
        resolver = new RentalTerminationResolverImpl(calculationService);

        rental = mock(Rental.class);
        user = mock(User.class);
        scooter = mock(Scooter.class);
    }

    // Validation tests

    @Test
    void resolveTerminationReason_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        null,
                        user,
                        scooter,
                        10,
                        10,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenUserIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        null,
                        scooter,
                        10,
                        10,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        user,
                        null,
                        10,
                        10,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenRequestedReasonIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        user,
                        scooter,
                        10,
                        10,
                        null
                )
        );
    }

    // Subscription tests

    @Test
    void resolveTerminationReason_shouldReturnRequestedReason_forSubscription() {
        when(rental.getTariffType()).thenReturn(TariffType.SUBSCRIPTION);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                500,
                100,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.USER_FINISHED, result);
    }

    @Test
    void resolveTerminationReason_shouldReturnTechnicalBreakdown_forSubscription() {
        when(rental.getTariffType()).thenReturn(TariffType.SUBSCRIPTION);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                500,
                100,
                TerminationReason.TECHNICAL_BREAKDOWN
        );

        assertEquals(TerminationReason.TECHNICAL_BREAKDOWN, result);
    }

    // Normal time tariff tests

    @Test
    void resolveTerminationReason_shouldReturnRequestedReason_whenActualMinutesDoNotExceedEffectiveMinutes() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(100);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80,
                100,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.USER_FINISHED, result);
    }

    @Test
    void resolveTerminationReason_shouldReturnRequestedReason_whenActualMinutesEqualEffectiveMinutes() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(100);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                100,
                100,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.USER_FINISHED, result);
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenTimeTariffHasNullLimit() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        user,
                        scooter,
                        120,
                        100,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenTimeTariffHasZeroLimit() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(0);

        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        user,
                        scooter,
                        120,
                        100,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    @Test
    void resolveTerminationReason_shouldThrowException_whenTimeTariffHasNegativeLimit() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(-1);

        assertThrows(
                RentalValidationException.class,
                () -> resolver.resolveTerminationReason(
                        rental,
                        user,
                        scooter,
                        120,
                        100,
                        TerminationReason.USER_FINISHED
                )
        );
    }

    // Payment limit tests

    @Test
    void resolveTerminationReason_shouldReturnPaymentLimitExceeded_forMinuteTariff() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(50);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(100);

        when(calculationService.calculateMaxAllowedMinutes(user, scooter))
                .thenReturn(50);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80,
                50,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.PAYMENT_LIMIT_EXCEEDED, result);
    }

    @Test
    void resolveTerminationReason_shouldReturnPaymentLimitExceeded_forHourTariff() {
        when(rental.getTariffType()).thenReturn(TariffType.HOUR);
        when(rental.getMaxAllowedMinutes()).thenReturn(60);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(120);

        when(calculationService.calculateAvailableMinutesByMoneyForHour(user, scooter))
                .thenReturn(60);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                90,
                60,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.PAYMENT_LIMIT_EXCEEDED, result);
    }

    // Battery limit tests

    @Test
    void resolveTerminationReason_shouldReturnBatteryDepleted_forMinuteTariff() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(40);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(40);

        when(calculationService.calculateMaxAllowedMinutes(user, scooter))
                .thenReturn(100);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                70,
                40,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.BATTERY_DEPLETED, result);
    }

    @Test
    void resolveTerminationReason_shouldReturnBatteryDepleted_forHourTariff() {
        when(rental.getTariffType()).thenReturn(TariffType.HOUR);
        when(rental.getMaxAllowedMinutes()).thenReturn(30);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(30);

        when(calculationService.calculateAvailableMinutesByMoneyForHour(user, scooter))
                .thenReturn(100);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                60,
                30,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.BATTERY_DEPLETED, result);
    }

    // No automatic override tests

    @Test
    void resolveTerminationReason_shouldReturnRequestedReason_whenNoLimitReasonDetected_forMinuteTariff() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(10000));

        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(50);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(50);

        when(calculationService.calculateMaxAllowedMinutes(user, scooter))
                .thenReturn(50);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                50,
                50,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.USER_FINISHED, result);
    }

    @Test
    void resolveTerminationReason_shouldReturnRequestedReason_whenMaxAllowedMinutesDoesNotMatchMoneyOrBattery() {
        when(rental.getTariffType()).thenReturn(TariffType.MINUTE);
        when(rental.getMaxAllowedMinutes()).thenReturn(70);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(100);

        when(calculationService.calculateMaxAllowedMinutes(user, scooter))
                .thenReturn(50);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                90,
                70,
                TerminationReason.USER_FINISHED
        );

        assertEquals(TerminationReason.USER_FINISHED, result);
    }

    @Test
    void resolveTerminationReason_shouldPreserveRequestedReason_whenRequestedReasonIsTechnicalBreakdownAndNoLimitDetected() {
        when(rental.getTariffType()).thenReturn(TariffType.HOUR);
        when(rental.getMaxAllowedMinutes()).thenReturn(90);

        when(calculationService.calculateAvailableMinutesByBattery(scooter))
                .thenReturn(120);

        when(calculationService.calculateAvailableMinutesByMoneyForHour(user, scooter))
                .thenReturn(100);

        TerminationReason result = resolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                120,
                90,
                TerminationReason.TECHNICAL_BREAKDOWN
        );

        assertEquals(TerminationReason.TECHNICAL_BREAKDOWN, result);
    }
}