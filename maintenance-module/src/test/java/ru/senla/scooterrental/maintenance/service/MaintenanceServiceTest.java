package ru.senla.scooterrental.maintenance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.repository.ServiceEventRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private ServiceEventRepository serviceEventRepository;

    @Mock
    private FleetService fleetService;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private Scooter scooter;

    @BeforeEach
    void setUp() {
        ScooterModel scooterModel = mock(ScooterModel.class);
        RentalPoint rentalPoint = mock(RentalPoint.class);

        when(scooterModel.getBatteryCapacity()).thenReturn(1000);
        when(rentalPoint.canAcceptScooter()).thenReturn(true);

        scooter = new Scooter(
                scooterModel,
                rentalPoint,
                100.0
        );
    }

    @Test
    void reportTechnicalBreakdown_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.reportTechnicalBreakdown(
                1L,
                "Сломано колесо"
        );

        assertEquals(ServiceEventType.TECHNICAL_BREAKDOWN, event.getType());
        assertEquals("Сломано колесо", event.getDescription());

        verify(fleetService).markServiceRequired(1L);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void reportUserDamage_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.reportUserDamage(
                1L,
                "Пользователь повредил руль"
        );

        assertEquals(ServiceEventType.USER_DAMAGE, event.getType());
        assertEquals("Пользователь повредил руль", event.getDescription());

        verify(fleetService).markServiceRequired(1L);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void sendToMaintenance_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.sendToMaintenance(
                1L,
                "Передан в сервисный центр"
        );

        assertEquals(ServiceEventType.SENT_TO_MAINTENANCE, event.getType());
        assertEquals("Передан в сервисный центр", event.getDescription());

        verify(fleetService).sendToMaintenance(1L);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void completeMaintenance_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.completeMaintenance(
                1L,
                "Техническое обслуживание завершено"
        );

        assertEquals(ServiceEventType.MAINTENANCE_COMPLETED, event.getType());
        assertEquals("Техническое обслуживание завершено", event.getDescription());

        verify(fleetService).completeMaintenance(1L);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void chargeScooter_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.chargeScooter(
                1L,
                50.0,
                "Самокат заряжен"
        );

        assertEquals(ServiceEventType.CHARGED, event.getType());
        assertEquals("Самокат заряжен", event.getDescription());

        verify(fleetService).chargeScooter(1L, 50.0);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void chargeScooter_shouldThrowException_whenAmountIsNegative() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.chargeScooter(
                        1L,
                        -10.0,
                        "Некорректная зарядка"
                )
        );

        assertEquals(
                "Объем зарядки должен быть положительным",
                exception.getMessage()
        );

        verify(fleetService, never()).chargeScooter(1L, -10.0);
        verify(serviceEventRepository, never()).save(any(ScooterServiceEvent.class));
    }

    @Test
    void chargeScooter_shouldThrowException_whenAmountIsZero() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.chargeScooter(
                        1L,
                        0.0,
                        "Некорректная зарядка"
                )
        );

        assertEquals(
                "Объем зарядки должен быть положительным",
                exception.getMessage()
        );

        verify(fleetService, never()).chargeScooter(1L, 0.0);
        verify(serviceEventRepository, never()).save(any(ScooterServiceEvent.class));
    }

    @Test
    void markServiceRequired_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.markServiceRequired(
                1L,
                "Проблема с аккумулятором"
        );

        assertEquals(ServiceEventType.SERVICE_REQUIRED, event.getType());
        assertEquals("Проблема с аккумулятором", event.getDescription());

        verify(fleetService).markServiceRequired(1L);
        verify(fleetService).getScooterById(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void getAllEvents_shouldReturnAllEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.CHARGED,
                "Самокат заряжен"
        );

        when(serviceEventRepository.findAll())
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getAllEvents();

        assertEquals(1, events.size());
        assertEquals(ServiceEventType.CHARGED, events.get(0).getType());

        verify(serviceEventRepository).findAll();
    }

    @Test
    void getEventsByScooterId_shouldReturnEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.CHARGED,
                "Самокат заряжен"
        );

        when(serviceEventRepository.findAllByScooterId(1L))
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getEventsByScooterId(1L);

        assertEquals(1, events.size());
        assertEquals(ServiceEventType.CHARGED, events.get(0).getType());

        verify(serviceEventRepository).findAllByScooterId(1L);
    }

    @Test
    void getEventsByScooterId_shouldThrowException_whenIdIsNull() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.getEventsByScooterId(null)
        );

        assertEquals(
                "ID самоката должен быть положительным",
                exception.getMessage()
        );

        verify(serviceEventRepository, never()).findAllByScooterId(any());
    }

    @Test
    void getEventsByScooterId_shouldThrowException_whenIdIsInvalid() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.getEventsByScooterId(0L)
        );

        assertEquals(
                "ID самоката должен быть положительным",
                exception.getMessage()
        );

        verify(serviceEventRepository, never()).findAllByScooterId(0L);
    }

    @Test
    void getEventsByType_shouldReturnEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.USER_DAMAGE,
                "Повреждение"
        );

        when(serviceEventRepository.findAllByType(ServiceEventType.USER_DAMAGE))
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getEventsByType(ServiceEventType.USER_DAMAGE);

        assertEquals(1, events.size());
        assertEquals(ServiceEventType.USER_DAMAGE, events.get(0).getType());

        verify(serviceEventRepository).findAllByType(ServiceEventType.USER_DAMAGE);
    }

    @Test
    void getEventsByType_shouldThrowException_whenTypeIsNull() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.getEventsByType(null)
        );

        assertEquals(
                "Тип сервисного события не может быть пустым",
                exception.getMessage()
        );

        verify(serviceEventRepository, never()).findAllByType(any());
    }
}