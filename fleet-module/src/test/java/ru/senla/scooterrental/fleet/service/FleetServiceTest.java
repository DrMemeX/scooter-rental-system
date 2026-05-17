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
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FleetServiceTest {

    @Mock
    private LocationNodeRepository locationNodeRepository;

    @Mock
    private RentalPointRepository rentalPointRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @Mock
    private ScooterModelRepository scooterModelRepository;

    @InjectMocks
    private FleetService fleetService;

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
    void createLocation_shouldCreateLocationWithoutParentSuccessfully() {
        when(locationNodeRepository.save(any(LocationNode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocationNode result = fleetService.createLocation(
                "Orel",
                LocationType.CITY,
                null
        );

        assertEquals("Orel", result.getName());
        assertEquals(LocationType.CITY, result.getType());

        verify(locationNodeRepository).save(any(LocationNode.class));
    }

    @Test
    void createLocation_shouldCreateLocationWithParentSuccessfully() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(any(LocationNode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocationNode result = fleetService.createLocation(
                "Central District",
                LocationType.DISTRICT,
                1L
        );

        assertEquals("Central District", result.getName());
        assertEquals(city, result.getParent());

        verify(locationNodeRepository).findById(1L);
        verify(locationNodeRepository).save(any(LocationNode.class));
    }

    @Test
    void createLocation_shouldThrowException_whenParentNotFound() {
        when(locationNodeRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> fleetService.createLocation(
                        "Central District",
                        LocationType.DISTRICT,
                        99L
                )
        );

        assertEquals(
                "Локация с ID 99 не найдена",
                exception.getMessage()
        );

        verify(locationNodeRepository, never()).save(any(LocationNode.class));
    }

    @Test
    void activateLocation_shouldActivateLocationSuccessfully() {
        city.deactivate();

        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = fleetService.activateLocation(1L);

        assertTrue(result.isActive());

        verify(locationNodeRepository).save(city);
    }

    @Test
    void deactivateLocation_shouldDeactivateLocationSuccessfully() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = fleetService.deactivateLocation(1L);

        assertFalse(result.isActive());

        verify(locationNodeRepository).save(city);
    }

    @Test
    void renameLocation_shouldRenameLocationSuccessfully() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = fleetService.renameLocation(
                1L,
                "New Orel"
        );

        assertEquals("New Orel", result.getName());

        verify(locationNodeRepository).save(city);
    }

    @Test
    void findAllLocations_shouldReturnAllLocations() {
        when(locationNodeRepository.findAll())
                .thenReturn(List.of(city, district, rentalPointNode));

        List<LocationNode> result = fleetService.findAllLocations();

        assertEquals(3, result.size());
        assertEquals(city, result.get(0));
        assertEquals(district, result.get(1));
        assertEquals(rentalPointNode, result.get(2));
    }

    @Test
    void findLocationsByType_shouldReturnLocationsByType() {
        when(locationNodeRepository.findAllByType(LocationType.CITY))
                .thenReturn(List.of(city));

        List<LocationNode> result = fleetService.findLocationsByType(LocationType.CITY);

        assertEquals(1, result.size());
        assertEquals(LocationType.CITY, result.get(0).getType());
    }

    @Test
    void createRentalPoint_shouldCreateRentalPointSuccessfully() {
        when(locationNodeRepository.findById(3L))
                .thenReturn(Optional.of(rentalPointNode));

        when(rentalPointRepository.save(any(RentalPoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RentalPoint result = fleetService.createRentalPoint(
                "Central Point 1",
                3L
        );

        assertEquals("Central Point 1", result.getName());
        assertEquals(rentalPointNode, result.getLocationNode());

        verify(rentalPointRepository).save(any(RentalPoint.class));
    }

    @Test
    void createRentalPoint_shouldThrowException_whenLocationIsNotRentalPoint() {
        when(locationNodeRepository.findById(2L))
                .thenReturn(Optional.of(district));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> fleetService.createRentalPoint(
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

        RentalPoint result = fleetService.activateRentalPoint(1L);

        assertTrue(result.isActive());

        verify(rentalPointRepository).save(rentalPoint);
    }

    @Test
    void deactivateRentalPoint_shouldDeactivateRentalPointSuccessfully() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(rentalPointRepository.save(rentalPoint))
                .thenReturn(rentalPoint);

        RentalPoint result = fleetService.deactivateRentalPoint(1L);

        assertFalse(result.isActive());

        verify(rentalPointRepository).save(rentalPoint);
    }

    @Test
    void renameRentalPoint_shouldRenameRentalPointSuccessfully() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(rentalPointRepository.save(rentalPoint))
                .thenReturn(rentalPoint);

        RentalPoint result = fleetService.renameRentalPoint(
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

        RentalPoint result = fleetService.getRentalPointById(1L);

        assertEquals(rentalPoint, result);
    }

    @Test
    void getActiveRentalPointById_shouldReturnRentalPoint_whenPointIsActive() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        RentalPoint result = fleetService.getActiveRentalPointById(1L);

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
                () -> fleetService.getActiveRentalPointById(1L)
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

        List<Scooter> result = fleetService.getRentalPointScooters(1L);

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void findAllRentalPoints_shouldReturnAllRentalPoints() {
        when(rentalPointRepository.findAll())
                .thenReturn(List.of(rentalPoint, secondRentalPoint));

        List<RentalPoint> result = fleetService.findAllRentalPoints();

        assertEquals(2, result.size());
        assertEquals(rentalPoint, result.get(0));
        assertEquals(secondRentalPoint, result.get(1));
    }

    @Test
    void findActiveRentalPoints_shouldReturnActiveRentalPoints() {
        when(rentalPointRepository.findAllActive())
                .thenReturn(List.of(rentalPoint));

        List<RentalPoint> result = fleetService.findActiveRentalPoints();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void deleteRentalPoint_shouldDeleteSuccessfully_whenNoScootersExist() {
        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of());

        fleetService.deleteRentalPoint(1L);

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
                () -> fleetService.deleteRentalPoint(1L)
        );

        assertEquals(
                "Нельзя удалить точку проката, пока в ней находятся самокаты",
                exception.getMessage()
        );

        verify(rentalPointRepository, never()).deleteById(1L);
    }

    @Test
    void createScooterModel_shouldCreateModelSuccessfully() {
        when(scooterModelRepository.save(any(ScooterModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterModel result = fleetService.createScooterModel(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        assertEquals(ScooterClass.BASIC, result.getScooterClass());
        assertEquals(20.0, result.getMaxSpeedKmPerHour());

        verify(scooterModelRepository).save(any(ScooterModel.class));
    }

    @Test
    void getScooterModelById_shouldReturnModel_whenModelExists() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        ScooterModel result = fleetService.getScooterModelById(1L);

        assertEquals(scooterModel, result);
    }

    @Test
    void updateScooterModelPrices_shouldUpdatePricesSuccessfully() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterModelRepository.save(scooterModel))
                .thenReturn(scooterModel);

        ScooterModel result = fleetService.updateScooterModelPrices(
                1L,
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(250)
        );

        assertEquals(BigDecimal.valueOf(5), result.getPricePerMinute());
        assertEquals(BigDecimal.valueOf(250), result.getPricePerHour());

        verify(scooterModelRepository).save(scooterModel);
    }

    @Test
    void findAllScooterModels_shouldReturnAllModels() {
        when(scooterModelRepository.findAll())
                .thenReturn(List.of(scooterModel));

        List<ScooterModel> result = fleetService.findAllScooterModels();

        assertEquals(1, result.size());
        assertEquals(scooterModel, result.get(0));
    }

    @Test
    void deleteScooterModel_shouldDeleteSuccessfully_whenScootersDoNotExist() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterRepository.findAll())
                .thenReturn(List.of());

        fleetService.deleteScooterModel(1L);

        verify(scooterModelRepository).deleteById(1L);
    }

    @Test
    void deleteScooterModel_shouldThrowException_whenScootersExist() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterRepository.findAll())
                .thenReturn(List.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> fleetService.deleteScooterModel(1L)
        );

        assertEquals(
                "Нельзя удалить модель самоката, пока существуют самокаты этой модели",
                exception.getMessage()
        );

        verify(scooterModelRepository, never()).deleteById(1L);
    }

    @Test
    void createScooter_shouldCreateScooterSuccessfully() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.save(any(Scooter.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Scooter result = fleetService.createScooter(
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
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> fleetService.createScooter(
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
    void rentScooter_shouldMarkScooterAsRentedSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.rentScooter(1L);

        assertEquals(ScooterStatus.RENTED, result.getStatus());
        assertNull(result.getCurrentRentalPoint());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void returnScooter_shouldReturnScooterToPointSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointRepository.findById(1L))
                .thenReturn(Optional.of(rentalPoint));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.returnScooter(1L, 1L);

        assertEquals(ScooterStatus.AVAILABLE, result.getStatus());
        assertEquals(rentalPoint, result.getCurrentRentalPoint());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void moveScooterToRentalPoint_shouldMoveScooterSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(rentalPointRepository.findById(2L))
                .thenReturn(Optional.of(secondRentalPoint));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.moveScooterToRentalPoint(1L, 2L);

        assertEquals(secondRentalPoint, result.getCurrentRentalPoint());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void requestReturnVerification_shouldChangeStatusSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.requestReturnVerification(1L);

        assertEquals(
                ScooterStatus.RETURN_VERIFICATION_REQUIRED,
                result.getStatus()
        );

        verify(scooterRepository).save(scooter);
    }

    @Test
    void sendToMaintenance_shouldChangeStatusSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.sendToMaintenance(1L);

        assertEquals(ScooterStatus.MAINTENANCE, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void completeMaintenance_shouldChangeStatusToAvailableSuccessfully() {
        scooter.sendToMaintenance();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.completeMaintenance(1L);

        assertEquals(ScooterStatus.AVAILABLE, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void markServiceRequired_shouldChangeStatusSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.markServiceRequired(1L);

        assertEquals(ScooterStatus.SERVICE_REQUIRED, result.getStatus());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void chargeScooter_shouldIncreaseChargeSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.chargeScooter(1L, 50.0);

        assertEquals(150.0, result.getCurrentCharge());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void chargeScooter_shouldThrowException_whenAmountIsNegative() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> fleetService.chargeScooter(1L, -10.0)
        );

        assertEquals(
                "Объем зарядки должен быть положительным",
                exception.getMessage()
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

        Scooter result = fleetService.consumeCharge(1L, 20);

        assertEquals(80.0, result.getCurrentCharge());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void addMileage_shouldIncreaseMileageSuccessfully() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        when(scooterRepository.save(scooter))
                .thenReturn(scooter);

        Scooter result = fleetService.addMileage(1L, 15);

        assertEquals(15.0, result.getTotalMileageKm());

        verify(scooterRepository).save(scooter);
    }

    @Test
    void getScooterById_shouldReturnScooter_whenScooterExists() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        Scooter result = fleetService.getScooterById(1L);

        assertEquals(scooter, result);
    }

    @Test
    void getScooterById_shouldThrowException_whenScooterNotFound() {
        when(scooterRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> fleetService.getScooterById(99L)
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

        List<Scooter> result = fleetService.findAllScooters();

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void findAvailableScooters_shouldReturnAvailableScooters() {
        when(scooterRepository.findAllAvailable())
                .thenReturn(List.of(scooter));

        List<Scooter> result = fleetService.findAvailableScooters();

        assertEquals(1, result.size());
        assertEquals(ScooterStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void findScootersByStatus_shouldReturnScootersByStatus() {
        when(scooterRepository.findAllByStatus(ScooterStatus.AVAILABLE))
                .thenReturn(List.of(scooter));

        List<Scooter> result =
                fleetService.findScootersByStatus(ScooterStatus.AVAILABLE);

        assertEquals(1, result.size());
        assertEquals(ScooterStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void findScootersByRentalPoint_shouldReturnScootersFromRentalPoint() {
        when(scooterRepository.findAllByRentalPointId(1L))
                .thenReturn(List.of(scooter));

        List<Scooter> result = fleetService.findScootersByRentalPoint(1L);

        assertEquals(1, result.size());
        assertEquals(scooter, result.get(0));
    }

    @Test
    void deleteScooter_shouldDeleteSuccessfully() {
        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        fleetService.deleteScooter(1L);

        verify(scooterRepository).deleteById(1L);
    }

    @Test
    void deleteScooter_shouldThrowException_whenScooterIsRented() {
        scooter.markAsRented();

        when(scooterRepository.findById(1L))
                .thenReturn(Optional.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> fleetService.deleteScooter(1L)
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
                () -> fleetService.deleteScooter(1L)
        );

        assertEquals(
                "Нельзя удалить самокат, участвующий в активной аренде",
                exception.getMessage()
        );

        verify(scooterRepository, never()).deleteById(1L);
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