package ru.senla.scooterrental.rental.service.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.service.impl.calculator.PricingServiceImpl;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingServiceTest {

    private PricingServiceImpl pricingServiceImpl;

    private User user;
    private Scooter scooter;
    private ScooterModel scooterModel;

    @BeforeEach
    void setUp() {
        pricingServiceImpl = new PricingServiceImpl();

        user = mock(User.class);
        scooter = mock(Scooter.class);
        scooterModel = mock(ScooterModel.class);

        when(scooter.getModel()).thenReturn(scooterModel);
    }

    // Validation tests

    @Test
    void calculate_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(null, scooter, null, 10)
        );
    }

    @Test
    void calculate_shouldThrowException_whenScooterIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, null, null, 10)
        );
    }

    @Test
    void calculate_shouldThrowException_whenScooterModelIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter, null, 10)
        );
    }

    @Test
    void calculate_shouldThrowException_whenTariffTypeIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        ReflectionTestUtils.setField(rental, "tariffType", null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter, null, 10)
        );
    }

    @Test
    void calculate_shouldThrowException_whenStartTimeIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(20);

        ReflectionTestUtils.setField(rental, "startTime", null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter)
        );
    }

    @Test
    void calculate_shouldThrowException_whenStartTimeIsInFuture() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(20);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now().plusMinutes(10)
        );

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter)
        );
    }

    // Minute tariff tests

    @Test
    void calculate_shouldCalculateMinutePrice() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(20);

        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                null,
                10
        );

        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    void calculate_shouldLimitMinutePriceByMaxAllowedMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(5);

        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                null,
                10
        );

        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void calculate_shouldCalculateMinimumOneMinutePrice() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(20);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now()
        );

        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        BigDecimal result = pricingServiceImpl.calculate(rental, scooter);

        assertEquals(BigDecimal.TEN, result);
    }

    @Test
    void calculate_shouldThrowException_whenMinuteTariffHasNoMaxAllowedMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter, null, 10)
        );
    }

    @Test
    void calculate_shouldThrowException_whenMinuteTariffMaxAllowedMinutesIsZero() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> rental.setMaxAllowedMinutes(0)
        );
    }

    @Test
    void calculate_shouldThrowException_whenMinuteTariffMaxAllowedMinutesIsNegative() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> rental.setMaxAllowedMinutes(-5)
        );
    }

    // Hour tariff tests

    @Test
    void calculate_shouldCalculateHourPrice() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 3);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(100));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                null,
                60
        );

        assertEquals(BigDecimal.valueOf(300), result);
    }

    @Test
    void calculate_shouldUseFullPackagePrice_whenHourRentalFinishedByUserInsidePlannedHours() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                TerminationReason.USER_FINISHED,
                80
        );

        assertEquals(BigDecimal.valueOf(1200), result);
    }

    @Test
    void calculate_shouldApplyOvertime_whenUserFinishedAfterPlannedHours() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.valueOf(12));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                TerminationReason.USER_FINISHED,
                150
        );

        assertEquals(BigDecimal.valueOf(1560), result);
    }

    @Test
    void calculate_shouldCalculateBatteryDepletedHourPrice() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 3);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                TerminationReason.BATTERY_DEPLETED,
                80
        );

        assertEquals(
                BigDecimal.valueOf(800.00).setScale(2),
                result.setScale(2)
        );
    }

    @Test
    void calculate_shouldApplyOvertime_whenBatteryDepletedAfterPlannedHours() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));
        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.valueOf(12));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                TerminationReason.BATTERY_DEPLETED,
                210
        );

        assertEquals(BigDecimal.valueOf(2280), result);
    }

    @Test
    void calculate_shouldUsePartialHourPrice_whenMaxAllowedMinutesLessThanPlannedMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);
        rental.setMaxAllowedMinutes(75);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(600));

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                TerminationReason.USER_FINISHED,
                100
        );

        assertEquals(
                BigDecimal.valueOf(750.00).setScale(2),
                result.setScale(2)
        );
    }

    @Test
    void calculate_shouldThrowException_whenHourTariffPlannedHoursIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> new Rental(user, scooter, TariffType.HOUR, null)
        );
    }

    @Test
    void calculate_shouldThrowException_whenHourTariffPlannedHoursIsZero() {
        assertThrows(
                RentalValidationException.class,
                () -> new Rental(user, scooter, TariffType.HOUR, 0)
        );
    }

    @Test
    void calculate_shouldThrowException_whenHourTariffPlannedHoursIsNegative() {
        assertThrows(
                RentalValidationException.class,
                () -> new Rental(user, scooter, TariffType.HOUR, -1)
        );
    }

    @Test
    void calculate_shouldThrowException_whenHourTariffHasInvalidPlannedHoursByReflection() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 1);

        ReflectionTestUtils.setField(rental, "plannedHours", null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingServiceImpl.calculate(rental, scooter, null, 60)
        );
    }

    // Subscription tariff tests

    @Test
    void calculate_shouldReturnZeroForSubscriptionTariff() {
        Rental rental = new Rental(user, scooter, TariffType.SUBSCRIPTION, null);

        BigDecimal result = pricingServiceImpl.calculate(
                rental,
                scooter,
                null,
                100
        );

        assertEquals(BigDecimal.ZERO, result);
    }
}