package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRentalPointControllerTest {

    private MockMvc mockMvc;
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        UserRentalPointController controller =
                new UserRentalPointController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getActiveRentalPoints_shouldReturnRentalPoints() throws Exception {
        RentalPoint first = rentalPoint(
                1L,
                "Central Point",
                true,
                10L,
                "Central District",
                LocationType.RENTAL_POINT
        );
        RentalPoint second = rentalPoint(
                2L,
                "North Point",
                true,
                20L,
                "North District",
                LocationType.RENTAL_POINT
        );

        when(fleetService.findActiveRentalPoints())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/rental-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Central Point"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].locationNodeId").value(10L))
                .andExpect(jsonPath("$[0].locationName").value("Central District"))
                .andExpect(jsonPath("$[0].locationType").value("RENTAL_POINT"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("North Point"))
                .andExpect(jsonPath("$[1].active").value(true))
                .andExpect(jsonPath("$[1].locationNodeId").value(20L))
                .andExpect(jsonPath("$[1].locationName").value("North District"))
                .andExpect(jsonPath("$[1].locationType").value("RENTAL_POINT"));

        verify(fleetService).findActiveRentalPoints();
    }

    @Test
    void getActiveRentalPoints_shouldReturnEmptyListSuccessfully()
            throws Exception {

        when(fleetService.findActiveRentalPoints())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/rental-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(fleetService).findActiveRentalPoints();
    }

    @Test
    void getRentalPointDetails_shouldReturnDetailsSuccessfully()
            throws Exception {

        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                true,
                10L,
                "Central District",
                LocationType.RENTAL_POINT
        );

        Scooter scooter = scooter(
                100L,
                ScooterStatus.AVAILABLE,
                90.0,
                15.5,
                5L,
                ScooterClass.BASIC,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400)
        );

        when(fleetService.getActiveRentalPointById(1L))
                .thenReturn(rentalPoint);

        when(fleetService.getRentalPointScooters(1L))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/rental-points/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalPoint.id").value(1L))
                .andExpect(jsonPath("$.rentalPoint.name").value("Central Point"))
                .andExpect(jsonPath("$.rentalPoint.active").value(true))
                .andExpect(jsonPath("$.rentalPoint.locationNodeId").value(10L))
                .andExpect(jsonPath("$.rentalPoint.locationName").value("Central District"))
                .andExpect(jsonPath("$.rentalPoint.locationType").value("RENTAL_POINT"))
                .andExpect(jsonPath("$.totalScooters").value(1))
                .andExpect(jsonPath("$.scooters[0].id").value(100L))
                .andExpect(jsonPath("$.scooters[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.scooters[0].currentCharge").value(90.0))
                .andExpect(jsonPath("$.scooters[0].totalMileageKm").value(15.5))
                .andExpect(jsonPath("$.scooters[0].modelId").value(5L))
                .andExpect(jsonPath("$.scooters[0].scooterClass").value("BASIC"))
                .andExpect(jsonPath("$.scooters[0].pricePerMinute").value(8))
                .andExpect(jsonPath("$.scooters[0].pricePerHour").value(400));

        verify(fleetService).getActiveRentalPointById(1L);
        verify(fleetService).getRentalPointScooters(1L);
    }

    @Test
    void getRentalPointDetails_shouldReturnDetailsWithEmptyScooters()
            throws Exception {

        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                true,
                10L,
                "Central District",
                LocationType.RENTAL_POINT
        );

        when(fleetService.getActiveRentalPointById(1L))
                .thenReturn(rentalPoint);

        when(fleetService.getRentalPointScooters(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/rental-points/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalPoint.id").value(1L))
                .andExpect(jsonPath("$.totalScooters").value(0))
                .andExpect(jsonPath("$.scooters").isArray())
                .andExpect(jsonPath("$.scooters").isEmpty());

        verify(fleetService).getActiveRentalPointById(1L);
        verify(fleetService).getRentalPointScooters(1L);
    }

    @Test
    void getRentalPointDetails_shouldReturnNotFound_whenRentalPointDoesNotExist()
            throws Exception {

        when(fleetService.getActiveRentalPointById(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Активная точка проката с ID 99 не найдена"
                ));

        mockMvc.perform(get("/api/v1/rental-points/99"))
                .andExpect(status().isNotFound());

        verify(fleetService).getActiveRentalPointById(99L);
    }

    @Test
    void getRentalPointDetails_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        when(fleetService.getActiveRentalPointById(0L))
                .thenThrow(new FleetValidationException(
                        "ID точки проката должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/rental-points/0"))
                .andExpect(status().isBadRequest());

        verify(fleetService).getActiveRentalPointById(0L);
    }

    @Test
    void getRentalPointDetails_shouldReturnBadRequest_whenRentalPointIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/rental-points/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    private RentalPoint rentalPoint(Long id,
                                    String name,
                                    boolean active,
                                    Long locationId,
                                    String locationName,
                                    LocationType locationType) {
        RentalPoint rentalPoint = mock(RentalPoint.class);
        LocationNode location = mock(LocationNode.class);

        lenient().when(rentalPoint.getId()).thenReturn(id);
        lenient().when(rentalPoint.getName()).thenReturn(name);
        lenient().when(rentalPoint.isActive()).thenReturn(active);
        lenient().when(rentalPoint.getLocationNode()).thenReturn(location);

        lenient().when(location.getId()).thenReturn(locationId);
        lenient().when(location.getName()).thenReturn(locationName);
        lenient().when(location.getType()).thenReturn(locationType);

        return rentalPoint;
    }

    private Scooter scooter(Long id,
                            ScooterStatus status,
                            Double currentCharge,
                            Double totalMileageKm,
                            Long modelId,
                            ScooterClass scooterClass,
                            BigDecimal pricePerMinute,
                            BigDecimal pricePerHour) {
        Scooter scooter = mock(Scooter.class);
        ScooterModel model = mock(ScooterModel.class);

        lenient().when(scooter.getId()).thenReturn(id);
        lenient().when(scooter.getStatus()).thenReturn(status);
        lenient().when(scooter.getCurrentCharge()).thenReturn(currentCharge);
        lenient().when(scooter.getTotalMileageKm()).thenReturn(totalMileageKm);
        lenient().when(scooter.getModel()).thenReturn(model);

        lenient().when(model.getId()).thenReturn(modelId);
        lenient().when(model.getScooterClass()).thenReturn(scooterClass);
        lenient().when(model.getPricePerMinute()).thenReturn(pricePerMinute);
        lenient().when(model.getPricePerHour()).thenReturn(pricePerHour);

        return scooter;
    }
}