package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.service.impl.FleetServiceImpl;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserScooterControllerTest {

    private MockMvc mockMvc;
    private FleetServiceImpl fleetService;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetServiceImpl.class);

        UserScooterController controller =
                new UserScooterController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAvailableScooters_shouldReturnScootersSuccessfully()
            throws Exception {

        Scooter first = scooter(
                1L,
                ScooterStatus.AVAILABLE,
                100.0,
                15.5,
                10L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000,
                100L,
                "Central Point"
        );

        Scooter second = scooter(
                2L,
                ScooterStatus.AVAILABLE,
                75.0,
                30.0,
                20L,
                ScooterClass.SPEEDY,
                30.0,
                3.0,
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(600),
                1200,
                200L,
                "North Point"
        );

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/scooters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].currentCharge").value(100.0))
                .andExpect(jsonPath("$[0].totalMileageKm").value(15.5))
                .andExpect(jsonPath("$[0].modelId").value(10L))
                .andExpect(jsonPath("$[0].scooterClass").value("BASIC"))
                .andExpect(jsonPath("$[0].maxSpeedKmPerHour").value(20.0))
                .andExpect(jsonPath("$[0].consumptionPerKm").value(2.0))
                .andExpect(jsonPath("$[0].pricePerMinute").value(8))
                .andExpect(jsonPath("$[0].pricePerHour").value(400))
                .andExpect(jsonPath("$[0].batteryCapacity").value(1000))
                .andExpect(jsonPath("$[0].rentalPointId").value(100L))
                .andExpect(jsonPath("$[0].rentalPointName")
                        .value("Central Point"))

                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].currentCharge").value(75.0))
                .andExpect(jsonPath("$[1].totalMileageKm").value(30.0))
                .andExpect(jsonPath("$[1].modelId").value(20L))
                .andExpect(jsonPath("$[1].scooterClass").value("SPEEDY"))
                .andExpect(jsonPath("$[1].maxSpeedKmPerHour").value(30.0))
                .andExpect(jsonPath("$[1].consumptionPerKm").value(3.0))
                .andExpect(jsonPath("$[1].pricePerMinute").value(12))
                .andExpect(jsonPath("$[1].pricePerHour").value(600))
                .andExpect(jsonPath("$[1].batteryCapacity").value(1200))
                .andExpect(jsonPath("$[1].rentalPointId").value(200L))
                .andExpect(jsonPath("$[1].rentalPointName")
                        .value("North Point"));

        verify(fleetService).findAvailableScooters();
    }

    @Test
    void getAvailableScooters_shouldReturnEmptyListSuccessfully()
            throws Exception {

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scooters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(fleetService).findAvailableScooters();
    }

    @Test
    void getAvailableScooters_shouldReturnScooterWithoutRentalPoint()
            throws Exception {

        Scooter scooter = scooter(
                1L,
                ScooterStatus.AVAILABLE,
                100.0,
                15.5,
                10L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000,
                null,
                null
        );

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/scooters/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].rentalPointId").doesNotExist())
                .andExpect(jsonPath("$[0].rentalPointName").doesNotExist());

        verify(fleetService).findAvailableScooters();
    }

    private Scooter scooter(Long id,
                            ScooterStatus status,
                            Double currentCharge,
                            Double totalMileageKm,
                            Long modelId,
                            ScooterClass scooterClass,
                            Double maxSpeedKmPerHour,
                            Double consumptionPerKm,
                            BigDecimal pricePerMinute,
                            BigDecimal pricePerHour,
                            Integer batteryCapacity,
                            Long rentalPointId,
                            String rentalPointName) {
        Scooter scooter = mock(Scooter.class);
        ScooterModel model = mock(ScooterModel.class);
        RentalPoint rentalPoint = rentalPointId == null
                ? null
                : mock(RentalPoint.class);

        lenient().when(scooter.getId()).thenReturn(id);
        lenient().when(scooter.getStatus()).thenReturn(status);
        lenient().when(scooter.getCurrentCharge()).thenReturn(currentCharge);
        lenient().when(scooter.getTotalMileageKm()).thenReturn(totalMileageKm);
        lenient().when(scooter.getModel()).thenReturn(model);
        lenient().when(scooter.getCurrentRentalPoint())
                .thenReturn(rentalPoint);

        lenient().when(model.getId()).thenReturn(modelId);
        lenient().when(model.getScooterClass()).thenReturn(scooterClass);
        lenient().when(model.getMaxSpeedKmPerHour())
                .thenReturn(maxSpeedKmPerHour);
        lenient().when(model.getConsumptionPerKm())
                .thenReturn(consumptionPerKm);
        lenient().when(model.getPricePerMinute())
                .thenReturn(pricePerMinute);
        lenient().when(model.getPricePerHour())
                .thenReturn(pricePerHour);
        lenient().when(model.getBatteryCapacity())
                .thenReturn(batteryCapacity);

        if (rentalPoint != null) {
            lenient().when(rentalPoint.getId()).thenReturn(rentalPointId);
            lenient().when(rentalPoint.getName()).thenReturn(rentalPointName);
        }

        return scooter;
    }
}