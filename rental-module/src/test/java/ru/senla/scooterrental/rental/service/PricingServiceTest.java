package ru.senla.scooterrental.rental.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PricingServiceTest {

    private PricingService pricingService;

    private User user;
    private Scooter scooter;
    private ScooterModel scooterModel;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();

        user = mock(User.class);
        scooter = mock(Scooter.class);
        scooterModel = mock(ScooterModel.class);

        when(scooter.getModel()).thenReturn(scooterModel);
    }

    @Test
    void calculate_shouldCalculateMinutePrice() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(20);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now().minusMinutes(10)
        );

        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        BigDecimal result = pricingService.calculate(rental, scooter);

        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    void calculate_shouldLimitMinutePriceByMaxAllowedMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);
        rental.setMaxAllowedMinutes(5);

        ReflectionTestUtils.setField(
                rental,
                "startTime",
                LocalDateTime.now().minusMinutes(10)
        );

        when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);

        BigDecimal result = pricingService.calculate(rental, scooter);

        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void calculate_shouldCalculateHourPrice() {
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 3);

        when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(100));

        BigDecimal result = pricingService.calculate(rental, scooter);

        assertEquals(BigDecimal.valueOf(300), result);
    }

    @Test
    void calculate_shouldReturnZeroForSubscriptionTariff() {
        Rental rental = new Rental(user, scooter, TariffType.SUBSCRIPTION, null);

        BigDecimal result = pricingService.calculate(rental, scooter);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculate_shouldThrowException_whenRentalIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> pricingService.calculate(null, scooter)
        );
    }

    @Test
    void calculate_shouldThrowException_whenScooterIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingService.calculate(rental, null)
        );
    }

    @Test
    void calculate_shouldThrowException_whenScooterModelIsNull() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        when(scooter.getModel()).thenReturn(null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingService.calculate(rental, scooter)
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
                () -> pricingService.calculate(rental, scooter)
        );
    }

    @Test
    void calculate_shouldThrowException_whenMinuteTariffHasNoMaxAllowedMinutes() {
        Rental rental = new Rental(user, scooter, TariffType.MINUTE, null);

        assertThrows(
                RentalValidationException.class,
                () -> pricingService.calculate(rental, scooter)
        );
    }
}