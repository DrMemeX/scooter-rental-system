package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserScooterModelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
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

        ScooterModel model = scooterModel();

        when(fleetService.findAllScooterModels())
                .thenReturn(List.of(model));

        mockMvc.perform(
                        get("/api/v1/scooter-models")
                )
                .andExpect(status().isOk());

        verify(fleetService).findAllScooterModels();
    }

    @Test
    void getScooterModelById_shouldReturnModelSuccessfully()
            throws Exception {

        ScooterModel model = scooterModel();

        when(fleetService.getScooterModelById(1L))
                .thenReturn(model);

        mockMvc.perform(
                        get("/api/v1/scooter-models/1")
                )
                .andExpect(status().isOk());

        verify(fleetService).getScooterModelById(1L);
    }

    private ScooterModel scooterModel() {
        ScooterModel model = mock(ScooterModel.class);

        lenient().when(model.getId()).thenReturn(1L);
        lenient().when(model.getScooterClass())
                .thenReturn(ru.senla.scooterrental.common.enums.ScooterClass.BASIC);
        lenient().when(model.getPricePerMinute())
                .thenReturn(BigDecimal.valueOf(10));
        lenient().when(model.getPricePerHour())
                .thenReturn(BigDecimal.valueOf(500));

        return model;
    }
}