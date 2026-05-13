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
                "Broken wheel"
        );

        assertEquals(ServiceEventType.TECHNICAL_BREAKDOWN, event.getType());
        assertEquals("Broken wheel", event.getDescription());

        verify(fleetService).markServiceRequired(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void chargeScooter_shouldThrowException_whenAmountIsNegative() {
        MaintenanceValidationException exception = assertThrows(
                MaintenanceValidationException.class,
                () -> maintenanceService.chargeScooter(
                        1L,
                        -10,
                        "Invalid charge"
                )
        );

        assertEquals(
                "Объем зарядки должен быть положительным",
                exception.getMessage()
        );
    }

    @Test
    void getEventsByScooterId_shouldReturnEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.CHARGED,
                "Charged"
        );

        when(serviceEventRepository.findAllByScooterId(1L))
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getEventsByScooterId(1L);

        assertEquals(1, events.size());
        assertEquals(ServiceEventType.CHARGED, events.get(0).getType());
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
    }

    @Test
    void getEventsByType_shouldReturnEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.USER_DAMAGE,
                "Damage"
        );

        when(serviceEventRepository.findAllByType(ServiceEventType.USER_DAMAGE))
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getEventsByType(ServiceEventType.USER_DAMAGE);

        assertEquals(1, events.size());
        assertEquals(ServiceEventType.USER_DAMAGE, events.get(0).getType());
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
    }

    @Test
    void completeMaintenance_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.completeMaintenance(
                1L,
                "Maintenance completed"
        );

        assertEquals(ServiceEventType.MAINTENANCE_COMPLETED, event.getType());

        verify(fleetService).completeMaintenance(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void markServiceRequired_shouldCreateEventSuccessfully() {
        when(fleetService.getScooterById(1L)).thenReturn(scooter);
        when(serviceEventRepository.save(any(ScooterServiceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterServiceEvent event = maintenanceService.markServiceRequired(
                1L,
                "Battery issue"
        );

        assertEquals(ServiceEventType.SERVICE_REQUIRED, event.getType());

        verify(fleetService).markServiceRequired(1L);
        verify(serviceEventRepository).save(any(ScooterServiceEvent.class));
    }

    @Test
    void getAllEvents_shouldReturnAllEventsSuccessfully() {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                ServiceEventType.CHARGED,
                "Charged"
        );

        when(serviceEventRepository.findAll())
                .thenReturn(List.of(event));

        List<ScooterServiceEvent> events =
                maintenanceService.getAllEvents();

        assertEquals(1, events.size());
    }
}