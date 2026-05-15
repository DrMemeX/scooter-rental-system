package ru.senla.scooterrental.rental.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.discount.service.DiscountService;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private FleetService fleetService;

    @Mock
    private PricingService pricingService;

    @Mock
    private DiscountService discountService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RentalService rentalService;

    @Test
    void startRental_shouldStartMinuteRentalSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(1L, 10L, TariffType.MINUTE);

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.MINUTE, result.getTariffType());
        assertEquals(100, result.getMaxAllowedMinutes());

        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldStartHourRentalSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(1L, 10L, TariffType.HOUR, 2);

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.HOUR, result.getTariffType());
        assertEquals(2, result.getPlannedHours());

        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldStartSubscriptionRentalSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(user.hasActiveSubscription()).thenReturn(true);
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(1L, 10L, TariffType.SUBSCRIPTION);

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.SUBSCRIPTION, result.getTariffType());

        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldThrowException_whenUserIdIsInvalid() {
        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(0L, 10L, TariffType.MINUTE)
        );

        verifyNoInteractions(userService, fleetService, rentalRepository);
    }

    @Test
    void startRental_shouldThrowException_whenScooterIdIsInvalid() {
        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, -10L, TariffType.MINUTE)
        );

        verifyNoInteractions(userService, fleetService, rentalRepository);
    }

    @Test
    void startRental_shouldThrowException_whenTariffTypeIsNull() {
        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, null)
        );

        verifyNoInteractions(userService, fleetService, rentalRepository);
    }

    @Test
    void startRental_shouldThrowException_whenUserIsBlocked() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(user.isBlocked()).thenReturn(true);
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenUserAlreadyHasUnfinishedRental() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental existingRental = new Rental(user, scooter, TariffType.MINUTE, null);

        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.of(existingRental));

        assertThrows(
                ActiveRentalAlreadyExistsException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenMinuteRentalBalanceIsInsufficient() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(user.getBalance()).thenReturn(BigDecimal.valueOf(5));
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenHourRentalPlannedHoursIsInvalid() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.HOUR, 0)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenHourRentalBalanceIsInsufficient() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(user.getBalance()).thenReturn(BigDecimal.valueOf(50));
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.HOUR, 2)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenUserHasNoActiveSubscription() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        when(user.hasActiveSubscription()).thenReturn(false);
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.SUBSCRIPTION)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldFinishRentalSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), null))
                .thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishRental(100L, 5L);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(BigDecimal.valueOf(200), result.getTotalCost());
        assertEquals(TerminationReason.USER_FINISHED, result.getTerminationReason());

        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(200));
        verify(rentalRepository).save(rental);
    }

    @Test
    void finishRental_shouldFinishRentalWithPromoAndDistance() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.existsByUserIdAndPromoCodeCode(1L, "SALE10")).thenReturn(false);
        when(discountService.applyDiscount(BigDecimal.valueOf(200), "SALE10"))
                .thenReturn(BigDecimal.valueOf(180));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishRental(100L, 5L, 0.2, "SALE10");

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(BigDecimal.valueOf(180), result.getTotalCost());
        assertEquals(0.2, result.getDistanceKm());

        verify(fleetService).addMileage(10L, 0.2);
        verify(fleetService).consumeCharge(10L, 1.0);
        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(180));
        verify(rentalRepository).save(rental);
    }

    @Test
    void finishRental_shouldThrowException_whenRentalNotFound() {
        when(rentalRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                RentalNotFoundException.class,
                () -> rentalService.finishRental(100L, 5L)
        );

        verify(fleetService, never()).returnScooter(anyLong(), anyLong());
        verify(userService, never()).subtractBalance(anyLong(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldThrowException_whenDistanceIsNegative() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.finishRental(100L, 5L, -1.0, null)
        );

        verify(fleetService, never()).returnScooter(anyLong(), anyLong());
        verify(userService, never()).subtractBalance(anyLong(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldThrowException_whenPromoCodeWasAlreadyUsed() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.existsByUserIdAndPromoCodeCode(1L, "SALE10")).thenReturn(true);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.finishRental(100L, 5L, 0, "SALE10")
        );

        verify(discountService, never()).applyDiscount(any(), anyString());
        verify(fleetService, never()).returnScooter(anyLong(), anyLong());
        verify(userService, never()).subtractBalance(anyLong(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishDueToBatteryDepleted_shouldFinishWithBatteryReason() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), null))
                .thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishDueToBatteryDepleted(100L, 5L, null);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.BATTERY_DEPLETED, result.getTerminationReason());
    }

    @Test
    void finishDueToTechnicalBreakdown_shouldFinishWithTechnicalBreakdownReason() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), null))
                .thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishDueToTechnicalBreakdown(100L, 5L, null);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.TECHNICAL_BREAKDOWN, result.getTerminationReason());
    }

    @Test
    void finishDueToUserDamage_shouldFinishWithUserDamageReason() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(
                eq(rental),
                eq(scooter),
                any(TerminationReason.class)
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), null))
                .thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishDueToUserDamage(100L, 5L, null);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.USER_DAMAGE, result.getTerminationReason());
    }

    @Test
    void requestManualFinish_shouldRequestManualFinishSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.requestManualFinish(100L);

        assertEquals(RentalStatus.PENDING_MANAGER_CONFIRMATION, result.getStatus());

        verify(fleetService).requestReturnVerification(10L);
        verify(rentalRepository).save(rental);
    }

    @Test
    void requestManualFinish_shouldThrowException_whenRentalNotFound() {
        when(rentalRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                RentalNotFoundException.class,
                () -> rentalService.requestManualFinish(100L)
        );

        verify(fleetService, never()).requestReturnVerification(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void approveManualFinish_shouldApproveManualFinishSuccessfully() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);
        rental.requestManualFinish();

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(pricingService.calculate(rental, scooter)).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), null))
                .thenReturn(BigDecimal.valueOf(200));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.approveManualFinish(100L, 5L);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.MANAGER_CONFIRMED_RETURN, result.getTerminationReason());
        assertEquals(BigDecimal.valueOf(200), result.getTotalCost());

        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(200));
        verify(rentalRepository).save(rental);
    }

    @Test
    void getRentalOrThrow_shouldReturnRental_whenRentalExists() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));

        Rental result = rentalService.getRentalOrThrow(100L);

        assertSame(rental, result);
        verify(rentalRepository).findById(100L);
    }

    @Test
    void getRentalOrThrow_shouldThrowException_whenRentalDoesNotExist() {
        when(rentalRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                RentalNotFoundException.class,
                () -> rentalService.getRentalOrThrow(100L)
        );

        verify(rentalRepository).findById(100L);
    }

    @Test
    void getRentalOrThrow_shouldThrowException_whenIdIsInvalid() {
        assertThrows(
                RentalValidationException.class,
                () -> rentalService.getRentalOrThrow(0L)
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    void getAllRentals_shouldReturnAllRentals() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();

        Rental rental1 = new Rental(user, scooter, TariffType.HOUR, 1);
        Rental rental2 = new Rental(user, scooter, TariffType.HOUR, 2);

        when(rentalRepository.findAll()).thenReturn(List.of(rental1, rental2));

        List<Rental> result = rentalService.getAllRentals();

        assertEquals(2, result.size());
        verify(rentalRepository).findAll();
    }

    @Test
    void getRentalsByUserId_shouldReturnUserRentals() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 1);

        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental));

        List<Rental> result = rentalService.getRentalsByUserId(1L);

        assertEquals(1, result.size());
        verify(rentalRepository).findByUserId(1L);
    }

    @Test
    void getRentalsByScooterId_shouldReturnScooterRentals() {
        User user = defaultUser();
        Scooter scooter = defaultScooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 1);

        when(rentalRepository.findByScooterId(10L)).thenReturn(List.of(rental));

        List<Rental> result = rentalService.getRentalsByScooterId(10L);

        assertEquals(1, result.size());
        verify(rentalRepository).findByScooterId(10L);
    }

    private User defaultUser() {
        User user = mock(User.class);

        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getBalance()).thenReturn(BigDecimal.valueOf(1000));
        lenient().when(user.isBlocked()).thenReturn(false);
        lenient().when(user.hasActiveSubscription()).thenReturn(true);

        return user;
    }

    private Scooter defaultScooter() {
        Scooter scooter = mock(Scooter.class);
        ScooterModel scooterModel = mock(ScooterModel.class);

        lenient().when(scooter.getId()).thenReturn(10L);
        lenient().when(scooter.getModel()).thenReturn(scooterModel);
        lenient().when(scooter.getCurrentCharge()).thenReturn(100.0);

        lenient().when(scooterModel.getPricePerMinute()).thenReturn(BigDecimal.TEN);
        lenient().when(scooterModel.getPricePerHour()).thenReturn(BigDecimal.valueOf(100));
        lenient().when(scooterModel.getMaxSpeedKmPerHour()).thenReturn(20.0);
        lenient().when(scooterModel.getConsumptionPerKm()).thenReturn(2.0);

        return scooter;
    }
}