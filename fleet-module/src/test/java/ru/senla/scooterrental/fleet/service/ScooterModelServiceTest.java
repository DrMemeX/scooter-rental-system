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
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.impl.ScooterModelServiceImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScooterModelServiceTest {

    @Mock
    private ScooterModelRepository scooterModelRepository;

    @Mock
    private ScooterRepository scooterRepository;

    @InjectMocks
    private ScooterModelServiceImpl scooterModelService;

    private LocationNode city;
    private LocationNode district;
    private LocationNode rentalPointNode;
    private RentalPoint rentalPoint;
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
    void createScooterModel_shouldCreateModelSuccessfully() {
        when(scooterModelRepository.save(any(ScooterModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScooterModel result = scooterModelService.createScooterModel(
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

        ScooterModel result = scooterModelService.getScooterModelById(1L);

        assertEquals(scooterModel, result);
    }

    @Test
    void getScooterModelById_shouldThrowException_whenModelNotFound() {
        when(scooterModelRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> scooterModelService.getScooterModelById(99L)
        );

        assertEquals(
                "Модель самоката с ID 99 не найдена",
                exception.getMessage()
        );
    }

    @Test
    void updateScooterModelPrices_shouldUpdatePricesSuccessfully() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterModelRepository.save(scooterModel))
                .thenReturn(scooterModel);

        ScooterModel result = scooterModelService.updateScooterModelPrices(
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

        List<ScooterModel> result = scooterModelService.findAllScooterModels();

        assertEquals(1, result.size());
        assertEquals(scooterModel, result.get(0));
    }

    @Test
    void deleteScooterModel_shouldDeleteSuccessfully_whenScootersDoNotExist() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterRepository.findAllByModelId(1L))
                .thenReturn(List.of());

        scooterModelService.deleteScooterModel(1L);

        verify(scooterModelRepository).deleteById(1L);
    }

    @Test
    void deleteScooterModel_shouldThrowException_whenScootersExist() {
        when(scooterModelRepository.findById(1L))
                .thenReturn(Optional.of(scooterModel));

        when(scooterRepository.findAllByModelId(1L))
                .thenReturn(List.of(scooter));

        FleetValidationException exception = assertThrows(
                FleetValidationException.class,
                () -> scooterModelService.deleteScooterModel(1L)
        );

        assertEquals(
                "Нельзя удалить модель самоката, пока существуют самокаты этой модели",
                exception.getMessage()
        );

        verify(scooterModelRepository, never()).deleteById(1L);
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
