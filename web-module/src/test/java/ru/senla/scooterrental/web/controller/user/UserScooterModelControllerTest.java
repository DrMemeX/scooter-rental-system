package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
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

class UserScooterModelControllerTest {

    private MockMvc mockMvc;
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        UserScooterModelController controller =
                new UserScooterModelController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getScooterModels_shouldReturnModelsSuccessfully()
            throws Exception {

        ScooterModel first = scooterModel(
                1L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        ScooterModel second = scooterModel(
                2L,
                ScooterClass.SPEEDY,
                30.0,
                3.0,
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(600),
                1200
        );

        when(fleetService.findAllScooterModels())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/scooter-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].scooterClass")
                        .value("BASIC"))
                .andExpect(jsonPath("$[0].maxSpeedKmPerHour")
                        .value(20.0))
                .andExpect(jsonPath("$[0].consumptionPerKm")
                        .value(2.0))
                .andExpect(jsonPath("$[0].pricePerMinute")
                        .value(8))
                .andExpect(jsonPath("$[0].pricePerHour")
                        .value(400))
                .andExpect(jsonPath("$[0].batteryCapacity")
                        .value(1000))

                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].scooterClass")
                        .value("SPEEDY"))
                .andExpect(jsonPath("$[1].maxSpeedKmPerHour")
                        .value(30.0))
                .andExpect(jsonPath("$[1].consumptionPerKm")
                        .value(3.0))
                .andExpect(jsonPath("$[1].pricePerMinute")
                        .value(12))
                .andExpect(jsonPath("$[1].pricePerHour")
                        .value(600))
                .andExpect(jsonPath("$[1].batteryCapacity")
                        .value(1200));

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModels_shouldReturnEmptyListSuccessfully()
            throws Exception {

        when(fleetService.findAllScooterModels())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scooter-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModelById_shouldReturnModelSuccessfully()
            throws Exception {

        ScooterModel model = scooterModel(
                1L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        when(fleetService.getScooterModelById(1L))
                .thenReturn(model);

        mockMvc.perform(get("/api/v1/scooter-models/1"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.scooterClass")
                        .value("BASIC"))
                .andExpect(jsonPath("$.maxSpeedKmPerHour")
                        .value(20.0))
                .andExpect(jsonPath("$.consumptionPerKm")
                        .value(2.0))
                .andExpect(jsonPath("$.pricePerMinute")
                        .value(8))
                .andExpect(jsonPath("$.pricePerHour")
                        .value(400))
                .andExpect(jsonPath("$.batteryCapacity")
                        .value(1000));

        verify(fleetService).getScooterModelById(1L);
    }

    @Test
    void getScooterModelById_shouldReturnNotFound()
            throws Exception {

        when(fleetService.getScooterModelById(99L))
                .thenThrow(
                        new FleetEntityNotFoundException(
                                "Модель не найдена"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/scooter-models/99")
                )
                .andExpect(status().isNotFound());

        verify(fleetService)
                .getScooterModelById(99L);
    }

    @Test
    void getScooterModelById_shouldReturnBadRequest_whenValidationFails()
            throws Exception {

        when(fleetService.getScooterModelById(0L))
                .thenThrow(
                        new FleetValidationException(
                                "ID должен быть положительным"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/scooter-models/0")
                )
                .andExpect(status().isBadRequest());

        verify(fleetService)
                .getScooterModelById(0L);
    }

    @Test
    void getScooterModelById_shouldReturnBadRequest_whenIdTypeInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/scooter-models/abc")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    private ScooterModel scooterModel(Long id,
                                      ScooterClass scooterClass,
                                      Double maxSpeedKmPerHour,
                                      Double consumptionPerKm,
                                      BigDecimal pricePerMinute,
                                      BigDecimal pricePerHour,
                                      Integer batteryCapacity) {

        ScooterModel model = mock(ScooterModel.class);

        lenient().when(model.getId())
                .thenReturn(id);

        lenient().when(model.getScooterClass())
                .thenReturn(scooterClass);

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

        return model;
    }
}