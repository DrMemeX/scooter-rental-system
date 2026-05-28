package ru.senla.scooterrental.rental.service;

import org.junit.jupiter.api.BeforeEach;
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
import ru.senla.scooterrental.rental.service.calculator.PricingService;
import ru.senla.scooterrental.rental.service.calculator.RentalCalculationService;
import ru.senla.scooterrental.rental.service.impl.RentalServiceImpl;
import ru.senla.scooterrental.rental.service.resolver.RentalTerminationResolver;
import ru.senla.scooterrental.rental.service.validator.RentalValidator;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private RentalCalculationService calculationService;

    @Mock
    private RentalTerminationResolver terminationResolver;

    @Mock
    private RentalValidator rentalValidator;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private User user;
    private Scooter scooter;

    @BeforeEach
    void setUp() {
        user = defaultUser();
        scooter = defaultScooter();
    }

    // Start rental tests

    @Test
    void startRental_shouldStartMinuteRentalSuccessfully() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.empty());
        when(calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.MINUTE,
                null
        )).thenReturn(100);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(
                1L,
                10L,
                TariffType.MINUTE
        );

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.MINUTE, result.getTariffType());
        assertEquals(100, result.getMaxAllowedMinutes());

        verify(rentalValidator).validatePositiveId(1L, "ID пользователя");
        verify(rentalValidator).validatePositiveId(10L, "ID самоката");
        verify(rentalValidator).requireNonNull(TariffType.MINUTE, "Тип тарифа");
        verify(rentalValidator).ensureUserCanStartRental(user);
        verify(rentalValidator).ensureCanPayForRental(
                user,
                scooter,
                TariffType.MINUTE,
                null
        );
        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldStartHourRentalSuccessfully() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.empty());
        when(calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.HOUR,
                2
        )).thenReturn(120);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(
                1L,
                10L,
                TariffType.HOUR,
                2
        );

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.HOUR, result.getTariffType());
        assertEquals(2, result.getPlannedHours());
        assertEquals(120, result.getMaxAllowedMinutes());

        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldStartSubscriptionRentalSuccessfully() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.empty());
        when(calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.SUBSCRIPTION,
                null
        )).thenReturn(null);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.startRental(
                1L,
                10L,
                TariffType.SUBSCRIPTION
        );

        assertNotNull(result);
        assertEquals(RentalStatus.ACTIVE, result.getStatus());
        assertEquals(TariffType.SUBSCRIPTION, result.getTariffType());
        assertEquals(null, result.getMaxAllowedMinutes());

        verify(fleetService).rentScooter(10L);
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void startRental_shouldThrowException_whenUserIdValidationFails() {
        doThrow(new RentalValidationException("ID пользователя должен быть положительным"))
                .when(rentalValidator)
                .validatePositiveId(null, "ID пользователя");

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(null, 10L, TariffType.MINUTE)
        );

        verifyNoInteractions(userService, fleetService);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenScooterIdValidationFails() {
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);

            if (id.equals(-10L)) {
                throw new RentalValidationException(
                        "ID самоката должен быть положительным"
                );
            }

            return null;
        }).when(rentalValidator)
                .validatePositiveId(anyLong(), anyString());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, -10L, TariffType.MINUTE)
        );

        verifyNoInteractions(userService);
        verifyNoInteractions(fleetService);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenTariffTypeValidationFails() {
        doThrow(new RentalValidationException("Тип тарифа не задан"))
                .when(rentalValidator)
                .requireNonNull(null, "Тип тарифа");

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, null)
        );

        verifyNoInteractions(userService, fleetService);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenUserCannotStartRental() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);

        doThrow(new RentalValidationException(
                "Заблокированный пользователь не может начать аренду"
        )).when(rentalValidator).ensureUserCanStartRental(user);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenUserAlreadyHasUnfinishedRental() {
        Rental existingRental = new Rental(
                user,
                scooter,
                TariffType.MINUTE,
                null
        );

        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.of(existingRental));

        assertThrows(
                ActiveRentalAlreadyExistsException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldThrowException_whenPaymentValidationFails() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.empty());

        doThrow(new RentalValidationException(
                "Недостаточно средств для начала аренды"
        )).when(rentalValidator).ensureCanPayForRental(
                user,
                scooter,
                TariffType.MINUTE,
                null
        );

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        verify(fleetService, never()).rentScooter(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void startRental_shouldNotSaveRental_whenFleetRentFails() {
        when(userService.getById(1L)).thenReturn(user);
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(rentalRepository.findUnfinishedByUserId(1L))
                .thenReturn(Optional.empty());
        when(calculationService.calculateMaxAllowedMinutesForStart(
                user,
                scooter,
                TariffType.MINUTE,
                null
        )).thenReturn(100);

        doThrow(new RuntimeException("scooter unavailable"))
                .when(fleetService)
                .rentScooter(10L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rentalService.startRental(1L, 10L, TariffType.MINUTE)
        );

        assertEquals("scooter unavailable", exception.getMessage());

        verify(rentalRepository, never()).save(any());
    }

    // Finish rental tests

    @Test
    void finishRental_shouldFinishRentalSuccessfully() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareSuccessfulFinish(
                rental,
                TerminationReason.USER_FINISHED,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.finishRental(100L, 5L);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(BigDecimal.valueOf(200), result.getTotalCost());
        assertEquals(TerminationReason.USER_FINISHED, result.getTerminationReason());

        verify(rentalValidator).validateActiveRental(rental);
        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(200));
        verify(rentalRepository).save(rental);
    }

    @Test
    void finishRental_shouldFinishRentalWithPromoAndDistance() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L)).thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                TerminationReason.USER_FINISHED
        )).thenReturn(TerminationReason.USER_FINISHED);
        when(pricingService.calculate(
                rental,
                scooter,
                TerminationReason.USER_FINISHED,
                80L
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), "SALE10"))
                .thenReturn(BigDecimal.valueOf(180));
        when(calculationService.calculateChargeConsumption(scooter, 0.2))
                .thenReturn(1.0);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.finishRental(
                100L,
                5L,
                0.2,
                "SALE10"
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(BigDecimal.valueOf(180), result.getTotalCost());
        assertEquals(0.2, result.getDistanceKm());

        verify(rentalValidator).validatePromoCodeNotUsedByUser(1L, "SALE10");
        verify(fleetService).addMileage(10L, 0.2);
        verify(fleetService).consumeCharge(10L, 1.0);
        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(180));
        verify(rentalRepository).save(rental);
    }

    @Test
    void finishRental_shouldThrowException_whenRentalPointIdValidationFails() {
        doThrow(new RentalValidationException(
                "ID точки проката должен быть положительным"
        )).when(rentalValidator).validatePositiveId(null, "ID точки проката");

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.finishRental(100L, null)
        );

        verifyNoInteractions(fleetService, pricingService, discountService, userService);
        verify(rentalRepository, never()).save(any());
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
    void finishRental_shouldThrowException_whenRentalIsNotActive() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));

        doThrow(new RentalValidationException(
                "Операция доступна только для активной аренды"
        )).when(rentalValidator).validateActiveRental(rental);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.finishRental(100L, 5L)
        );

        verify(fleetService, never()).getScooterById(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldThrowException_whenRideDistanceValidationFails() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L)).thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                TerminationReason.USER_FINISHED
        )).thenReturn(TerminationReason.USER_FINISHED);

        doThrow(new RentalValidationException(
                "Дистанция поездки не может быть отрицательной"
        )).when(rentalValidator).validateRideDistance(scooter, -1.0, 80L);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.finishRental(100L, 5L, -1.0, null)
        );

        verify(pricingService, never()).calculate(any(), any(), any(), anyLong());
        verify(fleetService, never()).returnScooter(anyLong(), anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldThrowException_whenPromoCodeWasAlreadyUsed() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L)).thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                TerminationReason.USER_FINISHED
        )).thenReturn(TerminationReason.USER_FINISHED);
        when(pricingService.calculate(
                rental,
                scooter,
                TerminationReason.USER_FINISHED,
                80L
        )).thenReturn(BigDecimal.valueOf(200));

        doThrow(new RentalValidationException(
                "Пользователь уже использовал данный промокод"
        )).when(rentalValidator).validatePromoCodeNotUsedByUser(1L, "SALE10");

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
    void finishRental_shouldNotSave_whenFleetReturnFails() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareFinishUntilReturn(
                rental,
                TerminationReason.USER_FINISHED,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        doThrow(new RuntimeException("return failed"))
                .when(fleetService)
                .returnScooter(10L, 5L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rentalService.finishRental(100L, 5L)
        );

        assertEquals("return failed", exception.getMessage());

        verify(userService, never()).subtractBalance(anyLong(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finishRental_shouldNotSave_whenUserBalanceSubtractFails() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareFinishWithoutSave(
                rental,
                TerminationReason.USER_FINISHED,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        doThrow(new RuntimeException("balance failed"))
                .when(userService)
                .subtractBalance(1L, BigDecimal.valueOf(200));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> rentalService.finishRental(100L, 5L)
        );

        assertEquals("balance failed", exception.getMessage());

        verify(fleetService).returnScooter(10L, 5L);
        verify(rentalRepository, never()).save(any());
    }

    // Specific finish reason tests

    @Test
    void finishDueToBatteryDepleted_shouldFinishWithBatteryReason() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareSuccessfulFinish(
                rental,
                TerminationReason.BATTERY_DEPLETED,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.finishDueToBatteryDepleted(
                100L,
                5L,
                null
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.BATTERY_DEPLETED, result.getTerminationReason());
    }

    @Test
    void finishDueToTechnicalBreakdown_shouldFinishWithTechnicalBreakdownReason() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareSuccessfulFinish(
                rental,
                TerminationReason.TECHNICAL_BREAKDOWN,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.finishDueToTechnicalBreakdown(
                100L,
                5L,
                null
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.TECHNICAL_BREAKDOWN, result.getTerminationReason());
    }

    @Test
    void finishDueToPaymentLimitExceeded_shouldFinishWithPaymentLimitReason() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareSuccessfulFinish(
                rental,
                TerminationReason.PAYMENT_LIMIT_EXCEEDED,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.finishDueToPaymentLimitExceeded(
                100L,
                5L
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.PAYMENT_LIMIT_EXCEEDED, result.getTerminationReason());
    }

    @Test
    void finishDueToUserDamage_shouldFinishWithUserDamageReason() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        prepareSuccessfulFinish(
                rental,
                TerminationReason.USER_DAMAGE,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.finishDueToUserDamage(
                100L,
                5L,
                null
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.USER_DAMAGE, result.getTerminationReason());
    }

    // Manual finish tests

    @Test
    void requestManualFinish_shouldRequestManualFinishSuccessfully() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.requestManualFinish(100L);

        assertEquals(RentalStatus.PENDING_MANAGER_CONFIRMATION, result.getStatus());

        verify(rentalValidator).validateActiveRental(rental);
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
    void requestManualFinish_shouldThrowException_whenRentalIsNotActive() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));

        doThrow(new RentalValidationException(
                "Операция доступна только для активной аренды"
        )).when(rentalValidator).validateActiveRental(rental);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.requestManualFinish(100L)
        );

        verify(fleetService, never()).requestReturnVerification(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void approveManualFinish_shouldApproveManualFinishSuccessfully() {
        Rental rental = pendingManualFinishRental();

        prepareSuccessfulManualApproval(
                rental,
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(200)
        );

        Rental result = rentalService.approveManualFinish(100L, 5L);

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.MANAGER_CONFIRMED_RETURN, result.getTerminationReason());
        assertEquals(BigDecimal.valueOf(200), result.getTotalCost());

        verify(rentalValidator).validatePendingManualFinish(rental);
        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(200));
        verify(rentalRepository).save(rental);
    }

    @Test
    void approveManualFinish_shouldApproveWithPromoAndDistance() {
        Rental rental = pendingManualFinishRental();

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L)).thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                TerminationReason.MANAGER_CONFIRMED_RETURN
        )).thenReturn(TerminationReason.MANAGER_CONFIRMED_RETURN);
        when(pricingService.calculate(
                rental,
                scooter,
                TerminationReason.MANAGER_CONFIRMED_RETURN,
                80L
        )).thenReturn(BigDecimal.valueOf(200));
        when(discountService.applyDiscount(BigDecimal.valueOf(200), "SALE10"))
                .thenReturn(BigDecimal.valueOf(180));
        when(calculationService.calculateChargeConsumption(scooter, 0.2))
                .thenReturn(1.0);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Rental result = rentalService.approveManualFinish(
                100L,
                5L,
                0.2,
                "SALE10"
        );

        assertEquals(RentalStatus.FINISHED, result.getStatus());
        assertEquals(TerminationReason.MANAGER_CONFIRMED_RETURN, result.getTerminationReason());
        assertEquals(BigDecimal.valueOf(180), result.getTotalCost());
        assertEquals(0.2, result.getDistanceKm());

        verify(rentalValidator).validatePendingManualFinish(rental);
        verify(rentalValidator).validatePromoCodeNotUsedByUser(1L, "SALE10");
        verify(fleetService).addMileage(10L, 0.2);
        verify(fleetService).consumeCharge(10L, 1.0);
        verify(fleetService).returnScooter(10L, 5L);
        verify(userService).subtractBalance(1L, BigDecimal.valueOf(180));
        verify(rentalRepository).save(rental);
    }

    @Test
    void approveManualFinish_shouldThrowException_whenRentalNotFound() {
        when(rentalRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                RentalNotFoundException.class,
                () -> rentalService.approveManualFinish(100L, 5L)
        );

        verify(fleetService, never()).returnScooter(anyLong(), anyLong());
        verify(userService, never()).subtractBalance(anyLong(), any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void approveManualFinish_shouldThrowException_whenRentalIsNotPendingManualFinish() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));

        doThrow(new RentalValidationException(
                "Подтвердить можно только аренду, ожидающую проверки менеджером"
        )).when(rentalValidator).validatePendingManualFinish(rental);

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.approveManualFinish(100L, 5L)
        );

        verify(fleetService, never()).getScooterById(anyLong());
        verify(rentalRepository, never()).save(any());
    }

    // Query tests

    @Test
    void getRentalOrThrow_shouldReturnRental_whenRentalExists() {
        Rental rental = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));

        Rental result = rentalService.getRentalOrThrow(100L);

        assertSame(rental, result);

        verify(rentalValidator).validatePositiveId(100L, "ID аренды");
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
    void getRentalOrThrow_shouldThrowException_whenIdValidationFails() {
        doThrow(new RentalValidationException("ID аренды должен быть положительным"))
                .when(rentalValidator)
                .validatePositiveId(null, "ID аренды");

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.getRentalOrThrow(null)
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    void getAllRentals_shouldReturnAllRentals() {
        Rental rental1 = activeRental(TariffType.HOUR, 1);
        Rental rental2 = activeRental(TariffType.HOUR, 2);

        when(rentalRepository.findAll()).thenReturn(List.of(rental1, rental2));

        List<Rental> result = rentalService.getAllRentals();

        assertEquals(2, result.size());
        verify(rentalRepository).findAll();
    }

    @Test
    void getAllRentals_shouldReturnEmptyList_whenNoRentalsExist() {
        when(rentalRepository.findAll()).thenReturn(List.of());

        List<Rental> result = rentalService.getAllRentals();

        assertEquals(0, result.size());
        verify(rentalRepository).findAll();
    }

    @Test
    void getRentalsByUserId_shouldReturnUserRentals() {
        Rental rental = activeRental(TariffType.HOUR, 1);

        when(rentalRepository.findByUserId(1L)).thenReturn(List.of(rental));

        List<Rental> result = rentalService.getRentalsByUserId(1L);

        assertEquals(1, result.size());

        verify(rentalValidator).validatePositiveId(1L, "ID пользователя");
        verify(rentalRepository).findByUserId(1L);
    }

    @Test
    void getRentalsByUserId_shouldThrowException_whenUserIdValidationFails() {
        doThrow(new RentalValidationException(
                "ID пользователя должен быть положительным"
        )).when(rentalValidator).validatePositiveId(null, "ID пользователя");

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.getRentalsByUserId(null)
        );

        verifyNoInteractions(rentalRepository);
    }

    @Test
    void getRentalsByScooterId_shouldReturnScooterRentals() {
        Rental rental = activeRental(TariffType.HOUR, 1);

        when(rentalRepository.findByScooterId(10L)).thenReturn(List.of(rental));

        List<Rental> result = rentalService.getRentalsByScooterId(10L);

        assertEquals(1, result.size());

        verify(rentalValidator).validatePositiveId(10L, "ID самоката");
        verify(rentalRepository).findByScooterId(10L);
    }

    @Test
    void getRentalsByScooterId_shouldThrowException_whenScooterIdValidationFails() {
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);

            if (id.equals(-10L)) {
                throw new RentalValidationException(
                        "ID самоката должен быть положительным"
                );
            }

            return null;
        }).when(rentalValidator)
                .validatePositiveId(anyLong(), anyString());

        assertThrows(
                RentalValidationException.class,
                () -> rentalService.getRentalsByScooterId(-10L)
        );

        verify(rentalRepository, never()).findByScooterId(anyLong());
    }

    private void prepareSuccessfulFinish(Rental rental,
                                         TerminationReason reason,
                                         BigDecimal totalCost,
                                         BigDecimal finalCost) {
        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L))
                .thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                reason
        )).thenReturn(reason);
        when(pricingService.calculate(rental, scooter, reason, 80L))
                .thenReturn(totalCost);
        when(discountService.applyDiscount(totalCost, null))
                .thenReturn(finalCost);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepareSuccessfulManualApproval(Rental rental,
                                                 BigDecimal totalCost,
                                                 BigDecimal finalCost) {
        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L))
                .thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                TerminationReason.MANAGER_CONFIRMED_RETURN
        )).thenReturn(TerminationReason.MANAGER_CONFIRMED_RETURN);
        when(pricingService.calculate(
                rental,
                scooter,
                TerminationReason.MANAGER_CONFIRMED_RETURN,
                80L
        )).thenReturn(totalCost);
        when(discountService.applyDiscount(totalCost, null))
                .thenReturn(finalCost);
        when(rentalRepository.save(any(Rental.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepareFinishUntilReturn(Rental rental,
                                          TerminationReason reason,
                                          BigDecimal totalCost,
                                          BigDecimal finalCost) {
        when(rentalRepository.findById(100L)).thenReturn(Optional.of(rental));
        when(fleetService.getScooterById(10L)).thenReturn(scooter);
        when(calculationService.calculateActualMinutes(rental)).thenReturn(80L);
        when(calculationService.resolveEffectiveMinutes(rental, 80L))
                .thenReturn(80L);
        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                reason
        )).thenReturn(reason);
        when(pricingService.calculate(rental, scooter, reason, 80L))
                .thenReturn(totalCost);
        when(discountService.applyDiscount(totalCost, null))
                .thenReturn(finalCost);
    }

    private void prepareFinishWithoutSave(Rental rental,
                                          TerminationReason reason,
                                          BigDecimal totalCost,
                                          BigDecimal finalCost) {

        when(rentalRepository.findById(100L))
                .thenReturn(Optional.of(rental));

        when(fleetService.getScooterById(10L))
                .thenReturn(scooter);

        when(calculationService.calculateActualMinutes(rental))
                .thenReturn(80L);

        when(calculationService.resolveEffectiveMinutes(rental, 80L))
                .thenReturn(80L);

        when(terminationResolver.resolveTerminationReason(
                rental,
                user,
                scooter,
                80L,
                80L,
                reason
        )).thenReturn(reason);

        when(pricingService.calculate(
                rental,
                scooter,
                reason,
                80L
        )).thenReturn(totalCost);

        when(discountService.applyDiscount(totalCost, null))
                .thenReturn(finalCost);
    }

    private Rental activeRental(TariffType tariffType, Integer plannedHours) {
        Rental rental = new Rental(user, scooter, tariffType, plannedHours);

        if (tariffType == TariffType.MINUTE || tariffType == TariffType.HOUR) {
            rental.setMaxAllowedMinutes(100);
        }

        return rental;
    }

    private Rental pendingManualFinishRental() {
        Rental rental = activeRental(TariffType.HOUR, 2);
        rental.requestManualFinish();

        return rental;
    }

    private User defaultUser() {
        User mockedUser = mock(User.class);

        lenient().when(mockedUser.getId()).thenReturn(1L);
        lenient().when(mockedUser.getBalance())
                .thenReturn(BigDecimal.valueOf(1000));
        lenient().when(mockedUser.isBlocked()).thenReturn(false);
        lenient().when(mockedUser.hasActiveSubscription()).thenReturn(true);

        return mockedUser;
    }

    private Scooter defaultScooter() {
        Scooter mockedScooter = mock(Scooter.class);
        ScooterModel mockedScooterModel = mock(ScooterModel.class);

        lenient().when(mockedScooter.getId()).thenReturn(10L);
        lenient().when(mockedScooter.getModel()).thenReturn(mockedScooterModel);
        lenient().when(mockedScooter.getCurrentCharge()).thenReturn(100.0);

        lenient().when(mockedScooterModel.getPricePerMinute())
                .thenReturn(BigDecimal.TEN);
        lenient().when(mockedScooterModel.getPricePerHour())
                .thenReturn(BigDecimal.valueOf(100));
        lenient().when(mockedScooterModel.getMaxSpeedKmPerHour())
                .thenReturn(20.0);
        lenient().when(mockedScooterModel.getConsumptionPerKm())
                .thenReturn(2.0);

        return mockedScooter;
    }
}