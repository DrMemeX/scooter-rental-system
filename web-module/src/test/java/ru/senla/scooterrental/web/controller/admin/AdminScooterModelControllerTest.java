package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.CreateScooterModelRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.UpdateScooterModelPricesRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminScooterModelControllerTest {

    private MockMvc mockMvc;
    private FleetService fleetService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        AdminScooterModelController controller =
                new AdminScooterModelController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createScooterModel_shouldCreateSuccessfully() throws Exception {
        ScooterModel model = scooterModel(
                1L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        CreateScooterModelRequest request = new CreateScooterModelRequest(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        when(fleetService.createScooterModel(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        )).thenReturn(model);

        mockMvc.perform(
                        post("/api/v1/admin/scooter-models")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.scooterClass").value("BASIC"))
                .andExpect(jsonPath("$.maxSpeedKmPerHour").value(20.0))
                .andExpect(jsonPath("$.consumptionPerKm").value(2.0))
                .andExpect(jsonPath("$.pricePerMinute").value(8))
                .andExpect(jsonPath("$.pricePerHour").value(400))
                .andExpect(jsonPath("$.batteryCapacity").value(1000));

        verify(fleetService).createScooterModel(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );
    }

    @Test
    void createScooterModel_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        CreateScooterModelRequest request = new CreateScooterModelRequest(
                null,
                -20.0,
                -2.0,
                BigDecimal.valueOf(-8),
                BigDecimal.valueOf(-400),
                -1000
        );

        mockMvc.perform(
                        post("/api/v1/admin/scooter-models")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void createScooterModel_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/scooter-models")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void createScooterModel_shouldReturnBadRequest_whenScooterClassIsInvalid()
            throws Exception {

        String invalidJson = """
                {
                  "scooterClass": "WRONG_CLASS",
                  "maxSpeedKmPerHour": 20.0,
                  "consumptionPerKm": 2.0,
                  "pricePerMinute": 8,
                  "pricePerHour": 400,
                  "batteryCapacity": 1000
                }
                """;

        mockMvc.perform(
                        post("/api/v1/admin/scooter-models")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidJson)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void createScooterModel_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        CreateScooterModelRequest request = new CreateScooterModelRequest(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );

        when(fleetService.createScooterModel(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        )).thenThrow(new FleetValidationException(
                "Модель самоката некорректна"
        ));

        mockMvc.perform(
                        post("/api/v1/admin/scooter-models")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(fleetService).createScooterModel(
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(400),
                1000
        );
    }

    @Test
    void getScooterModels_shouldReturnSuccessfully() throws Exception {
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

        mockMvc.perform(get("/api/v1/admin/scooter-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].scooterClass").value("BASIC"))
                .andExpect(jsonPath("$[0].maxSpeedKmPerHour").value(20.0))
                .andExpect(jsonPath("$[0].consumptionPerKm").value(2.0))
                .andExpect(jsonPath("$[0].pricePerMinute").value(8))
                .andExpect(jsonPath("$[0].pricePerHour").value(400))
                .andExpect(jsonPath("$[0].batteryCapacity").value(1000))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].scooterClass").value("SPEEDY"))
                .andExpect(jsonPath("$[1].maxSpeedKmPerHour").value(30.0))
                .andExpect(jsonPath("$[1].consumptionPerKm").value(3.0))
                .andExpect(jsonPath("$[1].pricePerMinute").value(12))
                .andExpect(jsonPath("$[1].pricePerHour").value(600))
                .andExpect(jsonPath("$[1].batteryCapacity").value(1200));

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModels_shouldReturnEmptyListSuccessfully() throws Exception {
        when(fleetService.findAllScooterModels())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/scooter-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModelById_shouldReturnSuccessfully() throws Exception {
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

        mockMvc.perform(get("/api/v1/admin/scooter-models/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.scooterClass").value("BASIC"))
                .andExpect(jsonPath("$.maxSpeedKmPerHour").value(20.0))
                .andExpect(jsonPath("$.consumptionPerKm").value(2.0))
                .andExpect(jsonPath("$.pricePerMinute").value(8))
                .andExpect(jsonPath("$.pricePerHour").value(400))
                .andExpect(jsonPath("$.batteryCapacity").value(1000));

        verify(fleetService).getScooterModelById(1L);
    }

    @Test
    void getScooterModelById_shouldReturnNotFound_whenModelDoesNotExist()
            throws Exception {

        when(fleetService.getScooterModelById(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Модель самоката с ID 99 не найдена"
                ));

        mockMvc.perform(get("/api/v1/admin/scooter-models/99"))
                .andExpect(status().isNotFound());

        verify(fleetService).getScooterModelById(99L);
    }

    @Test
    void getScooterModelById_shouldReturnBadRequest_whenModelIdIsInvalid()
            throws Exception {

        when(fleetService.getScooterModelById(0L))
                .thenThrow(new FleetValidationException(
                        "ID модели должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/scooter-models/0"))
                .andExpect(status().isBadRequest());

        verify(fleetService).getScooterModelById(0L);
    }

    @Test
    void getScooterModelById_shouldReturnBadRequest_whenModelIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/scooter-models/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void updateScooterModelPrices_shouldUpdateSuccessfully()
            throws Exception {

        ScooterModel model = scooterModel(
                1L,
                ScooterClass.BASIC,
                20.0,
                2.0,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500),
                1000
        );

        UpdateScooterModelPricesRequest request =
                new UpdateScooterModelPricesRequest(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(500)
                );

        when(fleetService.updateScooterModelPrices(
                1L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        )).thenReturn(model);

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/1/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.scooterClass").value("BASIC"))
                .andExpect(jsonPath("$.pricePerMinute").value(10))
                .andExpect(jsonPath("$.pricePerHour").value(500));

        verify(fleetService).updateScooterModelPrices(
                1L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        );
    }

    @Test
    void updateScooterModelPrices_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        UpdateScooterModelPricesRequest request =
                new UpdateScooterModelPricesRequest(
                        BigDecimal.valueOf(-10),
                        BigDecimal.valueOf(-500)
                );

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/1/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void updateScooterModelPrices_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/1/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void updateScooterModelPrices_shouldReturnNotFound_whenModelDoesNotExist()
            throws Exception {

        UpdateScooterModelPricesRequest request =
                new UpdateScooterModelPricesRequest(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(500)
                );

        when(fleetService.updateScooterModelPrices(
                99L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        )).thenThrow(new FleetEntityNotFoundException(
                "Модель самоката с ID 99 не найдена"
        ));

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/99/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(fleetService).updateScooterModelPrices(
                99L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        );
    }

    @Test
    void updateScooterModelPrices_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        UpdateScooterModelPricesRequest request =
                new UpdateScooterModelPricesRequest(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(500)
                );

        when(fleetService.updateScooterModelPrices(
                0L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        )).thenThrow(new FleetValidationException(
                "ID модели должен быть положительным"
        ));

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/0/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(fleetService).updateScooterModelPrices(
                0L,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(500)
        );
    }

    @Test
    void updateScooterModelPrices_shouldReturnBadRequest_whenModelIdTypeIsInvalid()
            throws Exception {

        UpdateScooterModelPricesRequest request =
                new UpdateScooterModelPricesRequest(
                        BigDecimal.valueOf(10),
                        BigDecimal.valueOf(500)
                );

        mockMvc.perform(
                        patch("/api/v1/admin/scooter-models/abc/prices")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void deleteScooterModel_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/scooter-models/1"))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteScooterModel(1L);
    }

    @Test
    void deleteScooterModel_shouldReturnNotFound_whenModelDoesNotExist()
            throws Exception {

        org.mockito.Mockito.doThrow(new FleetEntityNotFoundException(
                "Модель самоката с ID 99 не найдена"
        )).when(fleetService).deleteScooterModel(99L);

        mockMvc.perform(delete("/api/v1/admin/scooter-models/99"))
                .andExpect(status().isNotFound());

        verify(fleetService).deleteScooterModel(99L);
    }

    @Test
    void deleteScooterModel_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        org.mockito.Mockito.doThrow(new FleetValidationException(
                "ID модели должен быть положительным"
        )).when(fleetService).deleteScooterModel(0L);

        mockMvc.perform(delete("/api/v1/admin/scooter-models/0"))
                .andExpect(status().isBadRequest());

        verify(fleetService).deleteScooterModel(0L);
    }

    @Test
    void deleteScooterModel_shouldReturnBadRequest_whenModelIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(delete("/api/v1/admin/scooter-models/abc"))
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

        lenient().when(model.getId()).thenReturn(id);
        lenient().when(model.getScooterClass()).thenReturn(scooterClass);
        lenient().when(model.getMaxSpeedKmPerHour()).thenReturn(maxSpeedKmPerHour);
        lenient().when(model.getConsumptionPerKm()).thenReturn(consumptionPerKm);
        lenient().when(model.getPricePerMinute()).thenReturn(pricePerMinute);
        lenient().when(model.getPricePerHour()).thenReturn(pricePerHour);
        lenient().when(model.getBatteryCapacity()).thenReturn(batteryCapacity);

        return model;
    }
}