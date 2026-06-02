package ru.senla.scooterrental.rental.service.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.service.impl.calculator.RentalCalculationServiceImpl;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RentalCalculationServiceTest {

    private RentalCalculationServiceImpl calculationService;

    private User user;
    private Scooter scooter;
    private ScooterModel scooterModel;

    @BeforeEach
    void setUp() {
        calculationService = new RentalCalculationServiceImpl();

        user = mock(User.class);
        scooter = mock(Scooter.class);
        scooterModel = mock(ScooterModel.class);

        when(scooter.getModel()).thenReturn(scooterModel);
    }

    // calculateActualMinutes

    @Test
    void calculateActualMinutes_shouldReturnActualMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now().minusMinutes(15)
        );

        long result = calculationService.calculateActualMinutes(rental);

        assertEquals(15, result);
    }

    @Test
    void calculateActualMinutes_shouldReturnAtLeastOneMinute() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now()
        );

        long result = calculationService.calculateActualMinutes(rental);

        assertEquals(1, result);
    }

    @Test
    void calculateActualMinutes_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateActualMinutes(null)
        );
    }

    @Test
    void calculateActualMinutes_shouldThrowException_whenStartTimeIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        ReflectionTestUtils.setField(rental, "startTime", null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateActualMinutes(rental)
        );
    }

    // resolveEffectiveMinutes

    @Test
    void resolveEffectiveMinutes_shouldReturnActualMinutes_forSubscription() {
        Rental rental = new Rental(user, scooter, TariffType.SUBSCRIPTION, null);

        long result = calculationService.resolveEffectiveMinutes(rental, 120);

        assertEquals(120, result);
    }

    @Test
    void resolveEffectiveMinutes_shouldReturnActualMinutes_whenWithinLimit() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(100);

        long result = calculationService.resolveEffectiveMinutes(rental, 60);

        assertEquals(60, result);
    }

    @Test
    void resolveEffectiveMinutes_shouldReturnMaxAllowedMinutes_whenActualExceedsLimit() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(30);

        long result = calculationService.resolveEffectiveMinutes(rental, 60);

        assertEquals(30, result);
    }

    @Test
    void resolveEffectiveMinutes_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.resolveEffectiveMinutes(null, 10)
        );
    }

    @Test
    void resolveEffectiveMinutes_shouldThrowException_whenTimeTariffHasNoLimit() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.resolveEffectiveMinutes(rental, 10)
        );
    }

    // calculateChargeConsumption

    @Test
    void calculateChargeConsumption_shouldCalculateCeiledConsumption() {
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.5);

        double result = calculationService.calculateChargeConsumption(
                scooter,
                3.2
        );

        assertEquals(8.0, result);
    }

    @Test
    void calculateChargeConsumption_shouldReturnZero_whenDistanceIsZero() {
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.5);

        double result = calculationService.calculateChargeConsumption(
                scooter,
                0.0
        );

        assertEquals(0.0, result);
    }

    @Test
    void calculateChargeConsumption_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateChargeConsumption(null, 1.0)
        );
    }

    @Test
    void calculateChargeConsumption_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateChargeConsumption(scooter, 1.0)
        );
    }

    // calculateMaxAllowedMinutes

    @Test
    void calculateMaxAllowedMinutes_shouldCalculateByUserBalance() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(100));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        int result = calculationService.calculateMaxAllowedMinutes(
                user,
                scooter
        );

        assertEquals(10, result);
    }

    @Test
    void calculateMaxAllowedMinutes_shouldUseIntegralDivision() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(105));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        int result = calculationService.calculateMaxAllowedMinutes(
                user,
                scooter
        );

        assertEquals(10, result);
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenUserIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(null, scooter)
        );
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(user, null)
        );
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(user, scooter)
        );
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenPricePerMinuteIsZero() {
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.ZERO);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(user, scooter)
        );
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenPricePerMinuteIsNegative() {
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.valueOf(-1));

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(user, scooter)
        );
    }

    @Test
    void calculateMaxAllowedMinutes_shouldThrowException_whenBalanceIsInsufficient() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(5));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutes(user, scooter)
        );
    }

    // calculateAvailableMinutesByBattery

    @Test
    void calculateAvailableMinutesByBattery_shouldCalculateAvailableMinutes() {
        when(scooter.getCurrentCharge()).thenReturn(100.0);
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(25.0);

        int result = calculationService.calculateAvailableMinutesByBattery(scooter);

        assertEquals(120, result);
    }

    @Test
    void calculateAvailableMinutesByBattery_shouldFloorResult() {
        when(scooter.getCurrentCharge()).thenReturn(100.0);
        when(scooterModel.getConsumptionPerKm()).thenReturn(3.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(20.0);

        int result = calculationService.calculateAvailableMinutesByBattery(scooter);

        assertEquals(100, result);
    }

    @Test
    void calculateAvailableMinutesByBattery_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateAvailableMinutesByBattery(null)
        );
    }

    @Test
    void calculateAvailableMinutesByBattery_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateAvailableMinutesByBattery(scooter)
        );
    }

    // calculateAvailableMinutesByMoneyForHour

    @Test
    void calculateAvailableMinutesByMoneyForHour_shouldCalculateAvailableMinutes() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(1200));
        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));

        int result = calculationService.calculateAvailableMinutesByMoneyForHour(
                user,
                scooter
        );

        assertEquals(120, result);
    }

    @Test
    void calculateAvailableMinutesByMoneyForHour_shouldUseIntegralDivision() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(605));
        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));

        int result = calculationService.calculateAvailableMinutesByMoneyForHour(
                user,
                scooter
        );

        assertEquals(60, result);
    }

    @Test
    void calculateAvailableMinutesByMoneyForHour_shouldThrowException_whenUserIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateAvailableMinutesByMoneyForHour(
                        null,
                        scooter
                )
        );
    }

    @Test
    void calculateAvailableMinutesByMoneyForHour_shouldThrowException_whenScooterIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateAvailableMinutesByMoneyForHour(
                        user,
                        null
                )
        );
    }

    @Test
    void calculateAvailableMinutesByMoneyForHour_shouldThrowException_whenScooterModelIsNull() {
        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateAvailableMinutesByMoneyForHour(
                        user,
                        scooter
                )
        );
    }

    // calculateMaxAllowedMinutesForStart

    @Test
    void calculateMaxAllowedMinutesForStart_shouldReturnMinimumOfMoneyAndBattery_forMinuteTariff() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(1000));
        when(scooter.getCurrentCharge()).thenReturn(100.0);
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(50.0);

        Integer result = calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.MINUTE,
                null
        );

        assertEquals(60, result);
    }

    @Test
    void calculateMaxAllowedMinutesForStart_shouldReturnMinimumOfPlannedBatteryAndMoney_forHourTariff() {
        when(user.getBalance()).thenReturn(BigDecimal.valueOf(1000));
        when(scooter.getCurrentCharge()).thenReturn(100.0);
        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));
        when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);
        when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(25.0);

        Integer result = calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.HOUR,
                3
        );

        assertEquals(100, result);
    }

    @Test
    void calculateMaxAllowedMinutesForStart_shouldReturnNull_forSubscriptionTariff() {
        Integer result = calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.SUBSCRIPTION,
                null
        );

        assertEquals(null, result);
    }

    @Test
    void calculateMaxAllowedMinutesForStart_shouldThrowException_whenTariffTypeIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> calculationService.calculateMaxAllowedMinutesForStart(
                        user,
                        scooter,
                        null,
                        null
                )
        );
    }
}