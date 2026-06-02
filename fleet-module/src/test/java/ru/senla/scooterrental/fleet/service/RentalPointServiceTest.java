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
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.impl.RentalPointServiceImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalPointServiceTest {

    @Mock
    private RentalPointRepository rentalPointRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private RentalPointServiceImpl rentalPointService;

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
    void createRentalPoint_shouldCreateRentalPointSuccessfully() {
        when(locationService.getLocationById(3L))
                .thenReturn(rentalPointNode);

        when(rentalPointRepository.save(any(RentalPoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RentalPoint result = rentalPointService.createRentalPoint(
                "Central Point 1",
                3L
        );

        assertEquals("Central Point 1", result.getName());
        assertEquals(rentalPointNode, result.getLocationNode());

        verify(rentalPointRepository).save(any(RentalPoint.class));
    }

    @Test
    void createRentalPoint_shouldThrowException_whenLocationIsNotRentalPoint() {
        when(locationService.getLocationById(2L))
                .thenReturn(district);

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> rentalPointService.createRentalPoint(
                        "Wrong Point",
                        2L
                )
        );

        assertEquals(
                "Точка проката должна быть привязана к узлу типа RENTAL_POINT",
                exception.getMessage()
        );

        verify(rentalPointRepository, never()).save(any(RentalPoint.class));
    }

    @Test
    void activateRentalPoint_shouldActivateRentalPointSuccessfully() {
        rentalPoint.deactivate();

        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(rentalPointRepository.save(rentalPoint))
                .thenReturn(rentalPoint);

        RentalPoint result = rentalPointService.activateRentalPoint(1L);

        assertTrue(result.isActive());

        verify(rentalPointRepository).save(rentalPoint);
    }

    @Test
    void deactivateRentalPoint_shouldDeactivateRentalPointSuccessfully() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(rentalPointRepository.save(rentalPoint))
                .thenReturn(rentalPoint);

        RentalPoint result = rentalPointService.deactivateRentalPoint(1L);

        assertFalse(result.isActive());

        verify(rentalPointRepository).save(rentalPoint);
    }

    @Test
    void renameRentalPoint_shouldRenameRentalPointSuccessfully() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(rentalPointRepository.save(rentalPoint))
                .thenReturn(rentalPoint);

        RentalPoint result = rentalPointService.renameRentalPoint(
                1L,
                "Renamed Point"
        );

        assertEquals("Renamed Point", result.getName());

        verify(rentalPointRepository).save(rentalPoint);
    }

    @Test
    void getRentalPointById_shouldReturnRentalPoint_whenPointExists() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        RentalPoint result = rentalPointService.getRentalPointById(1L);

        assertEquals(rentalPoint, result);
    }

    @Test
    void getRentalPointById_shouldThrowException_whenPointNotFound() {
        when(rentalPointRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> rentalPointService.getRentalPointById(99L)
        );

        assertEquals(
                "Точка проката с ID 99 не найдена",
                exception.getMessage()
        );
    }

    @Test
    void getActiveRentalPointById_shouldReturnRentalPoint_whenPointIsActive() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        RentalPoint result = rentalPointService.getActiveRentalPointById(1L);

        assertEquals(rentalPoint, result);
        assertTrue(result.isActive());
    }

    @Test
    void getActiveRentalPointById_shouldThrowException_whenPointIsInactive() {
        rentalPoint.deactivate();

        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> rentalPointService.getActiveRentalPointById(1L)
        );

        assertEquals(
                "Точка проката с ID 1 недоступна",
                exception.getMessage()
        );
    }

    @Test
    void getRentalPointScooters_shouldReturnScootersFromRentalPoint() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of(scooter));

        List<Scooter> result = rentalPointService.getRentalPointScooters(1L);

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void findAllRentalPoints_shouldReturnAllRentalPoints() {
        when(rentalPointRepository.findAll())
                .thenReturn(List.of(rentalPoint, secondRentalPoint));

        List<RentalPoint> result = rentalPointService.findAllRentalPoints();

        assertEquals(2, result.size());
        assertEquals(rentalPoint, result.get(0));
        assertEquals(secondRentalPoint, result.get(1));
    }

    @Test
    void findActiveRentalPoints_shouldReturnActiveRentalPoints() {
        when(rentalPointRepository.findAllActive())
                .thenReturn(List.of(rentalPoint));

        List<RentalPoint> result = rentalPointService.findActiveRentalPoints();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void deleteRentalPoint_shouldDeleteSuccessfully_whenNoScootersExist() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of());

        rentalPointService.deleteRentalPoint(1L);

        verify(rentalPointRepository).deleteById(1L);
    }

    @Test
    void deleteRentalPoint_shouldThrowException_whenScootersExist() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> rentalPointService.deleteRentalPoint(1L)
        );

        assertEquals(
                "Нельзя удалить точку проката, пока в ней находятся самокаты",
                exception.getMessage()
        );

        verify(rentalPointRepository, never()).deleteById(1L);
    }

    @Test
    void findRentalPointsByLocationNode_shouldReturnRentalPoints() {
        when(locationService.getLocationById(3L))
                .thenReturn(rentalPointNode);

        when(rentalPointRepository.findAllByLocationNodeId(3L))
                .thenReturn(List.of(rentalPoint, secondRentalPoint));

        List<RentalPoint> result =
                rentalPointService.findRentalPointsByLocationNode(3L);

        assertEquals(2, result.size());
        assertEquals(rentalPoint, result.get(0));
        assertEquals(secondRentalPoint, result.get(1));

        verify(locationService).getLocationById(3L);
        verify(rentalPointRepository).findAllByLocationNodeId(3L);
    }

    @Test
    void findActiveRentalPointsByLocationNode_shouldReturnOnlyActiveRentalPoints() {
        secondRentalPoint.deactivate();

        when(locationService.getLocationById(3L))
                .thenReturn(rentalPointNode);

        when(rentalPointRepository.findAllByLocationNodeId(3L))
                .thenReturn(List.of(rentalPoint, secondRentalPoint));

        List<RentalPoint> result =
                rentalPointService.findActiveRentalPointsByLocationNode(3L);

        assertEquals(1, result.size());
        assertEquals(rentalPoint, result.get(0));
        assertTrue(result.get(0).isActive());

        verify(locationService).getLocationById(3L);
        verify(rentalPointRepository).findAllByLocationNodeId(3L);
    }

    @Test
    void findRentalPointsByLocationNode_shouldThrowException_whenLocationNotFound() {
        when(locationService.getLocationById(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Локация с ID 99 не найдена"
                ));

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> rentalPointService.findRentalPointsByLocationNode(99L)
        );

        assertEquals("Локация с ID 99 не найдена", exception.getMessage());

        verify(locationService).getLocationById(99L);
        verify(rentalPointRepository, never()).findAllByLocationNodeId(99L);
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
