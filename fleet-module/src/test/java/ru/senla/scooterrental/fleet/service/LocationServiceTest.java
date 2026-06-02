package ru.senla.scooterrental.fleet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;
import ru.senla.scooterrental.fleet.service.impl.LocationServiceImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationNodeRepository locationNodeRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private LocationNode city;
    private LocationNode district;
    private LocationNode rentalPointNode;

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
    }

    @Test
    void createLocation_shouldCreateLocationWithoutParentSuccessfully() {
        when(locationNodeRepository.save(any(LocationNode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocationNode result = locationService.createLocation(
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

        LocationNode result = locationService.createLocation(
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
                () -> locationService.createLocation(
                        "Central District",
                        LocationType.DISTRICT,
                        99L
                )
        );

        assertEquals("Локация с ID 99 не найдена", exception.getMessage());

        verify(locationNodeRepository, never()).save(any(LocationNode.class));
    }

    @Test
    void getLocationById_shouldReturnLocation_whenLocationExists() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        LocationNode result = locationService.getLocationById(1L);

        assertEquals(city, result);
    }

    @Test
    void getLocationById_shouldThrowException_whenLocationNotFound() {
        when(locationNodeRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> locationService.getLocationById(99L)
        );

        assertEquals("Локация с ID 99 не найдена", exception.getMessage());
    }

    @Test
    void activateLocation_shouldActivateLocationSuccessfully() {
        city.deactivate();

        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = locationService.activateLocation(1L);

        assertTrue(result.isActive());

        verify(locationNodeRepository).save(city);
    }

    @Test
    void deactivateLocation_shouldDeactivateLocationSuccessfully() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = locationService.deactivateLocation(1L);

        assertFalse(result.isActive());

        verify(locationNodeRepository).save(city);
    }

    @Test
    void renameLocation_shouldRenameLocationSuccessfully() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.save(city))
                .thenReturn(city);

        LocationNode result = locationService.renameLocation(
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

        List<LocationNode> result = locationService.findAllLocations();

        assertEquals(3, result.size());
        assertEquals(city, result.get(0));
        assertEquals(district, result.get(1));
        assertEquals(rentalPointNode, result.get(2));
    }

    @Test
    void findLocationsByType_shouldReturnLocationsByType() {
        when(locationNodeRepository.findAllByType(LocationType.CITY))
                .thenReturn(List.of(city));

        List<LocationNode> result = locationService.findLocationsByType(LocationType.CITY);

        assertEquals(1, result.size());
        assertEquals(LocationType.CITY, result.get(0).getType());
    }

    @Test
    void findChildLocations_shouldReturnChildLocations() {
        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.findAllByParentId(1L))
                .thenReturn(List.of(district));

        List<LocationNode> result = locationService.findChildLocations(1L);

        assertEquals(1, result.size());
        assertEquals(district, result.get(0));

        verify(locationNodeRepository).findById(1L);
        verify(locationNodeRepository).findAllByParentId(1L);
    }

    @Test
    void findActiveChildLocations_shouldReturnOnlyActiveChildLocations() {
        LocationNode inactiveDistrict = new LocationNode(
                "Inactive District",
                LocationType.DISTRICT,
                city
        );
        setId(inactiveDistrict, 4L);
        inactiveDistrict.deactivate();

        when(locationNodeRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(locationNodeRepository.findAllByParentId(1L))
                .thenReturn(List.of(district, inactiveDistrict));

        List<LocationNode> result = locationService.findActiveChildLocations(1L);

        assertEquals(1, result.size());
        assertEquals(district, result.get(0));
        assertTrue(result.get(0).isActive());

        verify(locationNodeRepository).findById(1L);
        verify(locationNodeRepository).findAllByParentId(1L);
    }

    @Test
    void findChildLocations_shouldThrowException_whenParentNotFound() {
        when(locationNodeRepository.findById(99L))
                .thenReturn(Optional.empty());

        FleetEntityNotFoundException exception = assertThrows(
                FleetEntityNotFoundException.class,
                () -> locationService.findChildLocations(99L)
        );

        assertEquals("Локация с ID 99 не найдена", exception.getMessage());

        verify(locationNodeRepository).findById(99L);
        verify(locationNodeRepository, never()).findAllByParentId(99L);
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
