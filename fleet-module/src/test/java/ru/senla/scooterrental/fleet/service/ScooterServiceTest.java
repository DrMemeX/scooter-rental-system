package ru.senla.scooterrental.fleet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.impl.ScooterServiceImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScooterServiceTest {

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private ScooterModelService scooterModelService;

    @Mock
    private RentalPointService rentalPointService;

    @InjectMocks
    private ScooterServiceImpl scooterService;

    private LocationNode city;
    private LocationNode district;
    private LocationNode rentalPointNode;
    private RentalPoint rentalPoint;
    private RentalPoint secondRentalPoint;
    private ScooterModel scooterModel;
    private Scooter scooter;

    @BeforeEach
    void setUp() {
        city = new LocationNode("Orel", LocationType.CITY, null);
        setId(city, 1L);

        district = new LocationNode("Central District", LocationType.DISTRICT, city);
        setId(district, 2L);

        rentalPointNode = new LocationNode(
                "Central Point 1",
                LocationType.RENTAL_POINT,
                district
        );
        setId(rentalPointNode, 3L);

        rentalPoint = new RentalPoint("Central Point 1", rentalPointNode);
        setId(rentalPoint, 1L);

        secondRentalPoint = new RentalPoint("Central Point 2", rentalPointNode);
        setId(secondRentalPoint, 2L);

        scooterModel = new ScooterModel(
                ScooterClass.SLOW,
                10.0,
                1.0,
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(200),
                1000
        );
        setId(scooterModel, 1L);

        scooter = new Scooter(scooterModel, rentalPoint, 100.0);
        setId(scooter, 1L);
    }

    @Test
    void createScooter_shouldCreateScooterSuccessfully() {
        when(scooterModelService.getScooterModelById(1L))
                .thenReturn(scooterModel);

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);

        when(scooterRepository.save(any(Scooter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Scooter result = scooterService.createScooter(
                1L,
                1L,
                100.0
        );

        assertNotNull(result);
        assertEquals(ScooterStatus.AVAILABLE, result.getStatus());
        assertEquals(100.0, result.getCurrentCharge());

        verify(scooterRepository).save(any(Scooter.class));
    }

    @Test
    void createScooter_shouldThrowException_whenChargeIsNegative() {
        when(scooterModelService.getScooterModelById(1L))
                .thenReturn(scooterModel);

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.createScooter(
                        1L,
                        1L,
                        -1.0
                )
        );

        assertEquals(
                "Заряд не может быть отрицательным",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void createScooter_shouldThrowException_whenModelNotFound() {
        when(scooterModelService.getScooterModelById(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Модель самоката с ID 99 не найдена"
                ));

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterService.createScooter(99L, 1L, 100.0)
        );

        assertEquals(
                "Модель самоката с ID 99 не найдена",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void createScooter_shouldThrowException_whenRentalPointNotFound() {
        when(scooterModelService.getScooterModelById(1L))
                .thenReturn(scooterModel);

        when(rentalPointService.getRentalPointById(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Точка проката с ID 99 не найдена"
                ));

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterService.createScooter(1L, 99L, 100.0)
        );

        assertEquals(
                "Точка проката с ID 99 не найдена",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void createScooter_shouldThrowException_whenChargeExceedsBatteryCapacity() {
        when(scooterModelService.getScooterModelById(1L))
                .thenReturn(scooterModel);

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.createScooter(1L, 1L, 2000.0)
        );

        assertEquals(
                "Заряд не может превышать емкость батареи",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void rentScooter_shouldMarkScooterAsRentedSuccessfully() {
        when(scooterRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.rentScooter(1L);

        assertEquals(ScooterStatus.RENTED, result.getStatus());
        assertNull(result.getCurrentRentalPoint());

        verify(scooterRepository).findByIdForUpdate(1L);
        verify(scooterRepository).save(scooter);
    }

    @Test
    void rentScooter_shouldThrowException_whenScooterNotFound() {
        when(scooterRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterService.rentScooter(99L)
        );

        assertEquals(
                "Самокат с ID 99 не найден",
                exception.getMessage()
        );

        verify(scooterRepository).findByIdForUpdate(99L);
        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void rentScooter_shouldThrowException_whenScooterIsNotAvailable() {
        scooter.sendToMaintenance();

        when(scooterRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.rentScooter(1L)
        );

        assertEquals(
                "Самокат с ID 1 недоступен для аренды",
                exception.getMessage()
        );

        verify(scooterRepository).findByIdForUpdate(1L);
        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void returnScooter_shouldReturnScooterToPointSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.returnScooter(1L, 1L);

        assertEquals(ScooterStatus.AVAILABLE, result.getStatus());
        assertEquals(rentalPoint, result.getCurrentRentalPoint());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void returnScooter_shouldThrowException_whenScooterIsNotRented() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.returnScooter(1L, 1L)
        );

        assertEquals(
                "Вернуть можно только арендованный самокат",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void moveScooterToRentalPoint_shouldMoveScooterSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointService.getRentalPointById(2L))
                .thenReturn(secondRentalPoint);

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.moveScooterToRentalPoint(1L, 2L);

        assertEquals(secondRentalPoint, result.getCurrentRentalPoint());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void moveScooterToRentalPoint_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointService.getRentalPointById(2L))
                .thenReturn(secondRentalPoint);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.moveScooterToRentalPoint(1L, 2L)
        );

        assertEquals(
                "Нельзя перемещать самокат с активной арендой",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void moveScooterToRentalPoint_shouldThrowException_whenScooterRequiresReturnVerification() {
        scooter.markAsRented();
        scooter.requireReturnVerification();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointService.getRentalPointById(2L))
                .thenReturn(secondRentalPoint);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.moveScooterToRentalPoint(1L, 2L)
        );

        assertEquals(
                "Нельзя перемещать самокат с активной арендой",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void requestReturnVerification_shouldChangeStatusSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.requestReturnVerification(1L);

        assertEquals(
                ScooterStatus.RETURN_VERIFICATION_REQUIRED,
                result.getStatus()
        );

        verify(scooterRepository).save(scooter);
    }

    @Test
    void requestReturnVerification_shouldThrowException_whenScooterIsNotRented() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.requestReturnVerification(1L)
        );

        assertEquals(
                "Запросить ручное завершение можно только для арендованного самоката",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void sendToMaintenance_shouldChangeStatusSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.sendToMaintenance(1L);

        assertEquals(ScooterStatus.MAINTENANCE, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void sendToMaintenance_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.sendToMaintenance(1L)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void completeMaintenance_shouldChangeStatusToAvailableSuccessfully() {
        scooter.sendToMaintenance();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.completeMaintenance(1L);

        assertEquals(ScooterStatus.AVAILABLE, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void completeMaintenance_shouldThrowException_whenScooterIsNotInMaintenance() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.completeMaintenance(1L)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void markServiceRequired_shouldChangeStatusSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.markServiceRequired(1L);

        assertEquals(ScooterStatus.SERVICE_REQUIRED, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void markServiceRequired_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.markServiceRequired(1L)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void chargeScooter_shouldIncreaseChargeSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.chargeScooter(1L, 50.0);

        assertEquals(150.0, result.getCurrentCharge());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void chargeScooter_shouldThrowException_whenAmountIsNegative() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.chargeScooter(1L, -10.0)
        );

        assertEquals(
                "Объем зарядки должен быть положительным",
                exception.getMessage()
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void chargeScooter_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.chargeScooter(1L, 50.0)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void consumeCharge_shouldDecreaseChargeSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.consumeCharge(1L, 20);

        assertEquals(80.0, result.getCurrentCharge());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void consumeCharge_shouldThrowException_whenScooterIsNotRented() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.consumeCharge(1L, 20.0)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void addMileage_shouldIncreaseMileageSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = scooterService.addMileage(1L, 15);

        assertEquals(15.0, result.getTotalMileageKm());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void addMileage_shouldThrowException_whenScooterIsNotRented() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        assertThrows(
                RuntimeException.class,
                () -> scooterService.addMileage(1L, 10.0)
        );

        verify(scooterRepository, never()).save(any(Scooter.class));
    }

    @Test
    void getScooterById_shouldReturnScooter_whenScooterExists() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        Scooter result = scooterService.getScooterById(1L);

        assertEquals(scooter, result);
    }

    @Test
    void getScooterById_shouldThrowException_whenScooterNotFound() {
        when(scooterRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterService.getScooterById(99L)
        );

        assertEquals(
                "Самокат с ID 99 не найден",
                exception.getMessage()
        );
    }

    @Test
    void findAllScooters_shouldReturnAllScooters() {
        when(scooterRepository.findAll())
                .thenReturn(List.of(scooter));

        List<Scooter> result = scooterService.findAllScooters();

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void findAvailableScooters_shouldReturnAvailableScooters() {
        when(scooterRepository.findAllAvailable())
                .thenReturn(List.of(scooter));

        List<Scooter> result = scooterService.findAvailableScooters();

        assertEquals(1, result.size());
        assertEquals(ScooterStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void findScootersByStatus_shouldReturnScootersByStatus() {
        when(scooterRepository.findAllByStatus(ScooterStatus.AVAILABLE))
                .thenReturn(List.of(scooter));

        List<Scooter> result =
                scooterService.findScootersByStatus(ScooterStatus.AVAILABLE);

        assertEquals(1, result.size());
        assertEquals(ScooterStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void findScootersByRentalPoint_shouldReturnScootersFromRentalPoint() {
        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of(scooter));

        List<Scooter> result = scooterService.findScootersByRentalPoint(1L);

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void deleteScooter_shouldDeleteSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        scooterService.deleteScooter(1L);

        verify(scooterRepository).deleteById(1L);
    }

    @Test
    void deleteScooter_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.deleteScooter(1L)
        );

        assertEquals(
                "Нельзя удалить самокат, участвующий в активной аренде",
                exception.getMessage()
        );

        verify(scooterRepository, never()).deleteById(1L);
    }

    @Test
    void deleteScooter_shouldThrowException_whenScooterRequiresReturnVerification() {
        scooter.markAsRented();
        scooter.requireReturnVerification();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterService.deleteScooter(1L)
        );

        assertEquals(
                "Нельзя удалить самокат, участвующий в активной аренде",
                exception.getMessage()
        );

        verify(scooterRepository, never()).deleteById(1L);
    }

    @Test
    void deleteScooter_shouldThrowException_whenScooterNotFound() {
        when(scooterRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterService.deleteScooter(99L)
        );

        assertEquals(
                "Самокат с ID 99 не найден",
                exception.getMessage()
        );

        verify(scooterRepository, never()).deleteById(99L);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot set test ID", exception);
        }
    }
}
