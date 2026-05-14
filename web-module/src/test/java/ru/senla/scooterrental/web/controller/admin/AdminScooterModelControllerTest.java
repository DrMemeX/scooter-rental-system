package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.CreateScooterModelRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scootermodel.UpdateScooterModelPricesRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        ScooterModel model = scooterModel();

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
                .andExpect(status().isCreated());
    }

    @Test
    void getScooterModels_shouldReturnSuccessfully() throws Exception {
        ScooterModel model = scooterModel();

        when(fleetService.findAllScooterModels())
                .thenReturn(List.of(model));

        mockMvc.perform(get("/api/v1/admin/scooter-models"))
                .andExpect(status().isOk());

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModelById_shouldReturnSuccessfully() throws Exception {
        ScooterModel model = scooterModel();

        when(fleetService.getScooterModelById(1L))
                .thenReturn(model);

        mockMvc.perform(get("/api/v1/admin/scooter-models/1"))
                .andExpect(status().isOk());

        verify(fleetService).getScooterModelById(1L);
    }

    @Test
    void updateScooterModelPrices_shouldUpdateSuccessfully()
            throws Exception {

        ScooterModel model = scooterModel();

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
                .andExpect(status().isOk());
    }

    @Test
    void deleteScooterModel_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/scooter-models/1"))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteScooterModel(1L);
    }

    private ScooterModel scooterModel() {
        ScooterModel model = mock(ScooterModel.class);

        lenient().when(model.getId()).thenReturn(1L);
        lenient().when(model.getScooterClass())
                .thenReturn(ScooterClass.BASIC);
        lenient().when(model.getMaxSpeedKmPerHour()).thenReturn(20.0);
        lenient().when(model.getConsumptionPerKm()).thenReturn(2.0);
        lenient().when(model.getPricePerMinute())
                .thenReturn(BigDecimal.valueOf(8));
        lenient().when(model.getPricePerHour())
                .thenReturn(BigDecimal.valueOf(400));
        lenient().when(model.getBatteryCapacity()).thenReturn(1000);

        return model;
    }
}