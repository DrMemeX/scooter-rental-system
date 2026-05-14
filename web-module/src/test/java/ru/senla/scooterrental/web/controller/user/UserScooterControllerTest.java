package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.Scooter;
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
class UserScooterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
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

        Scooter scooter = scooter();

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(
                        get("/api/v1/scooters/available")
                )
                .andExpect(status().isOk());

        verify(fleetService).findAvailableScooters();
    }

    private Scooter scooter() {
        Scooter scooter = mock(Scooter.class);
        ScooterModel model = mock(ScooterModel.class);

        lenient().when(scooter.getId()).thenReturn(1L);
        lenient().when(scooter.getModel()).thenReturn(model);
        lenient().when(scooter.getCurrentCharge()).thenReturn(100.0);

        lenient().when(model.getId()).thenReturn(1L);
        lenient().when(model.getScooterClass()).thenReturn(ScooterClass.BASIC);
        lenient().when(model.getPricePerMinute())
                .thenReturn(BigDecimal.valueOf(10));
        lenient().when(model.getPricePerHour())
                .thenReturn(BigDecimal.valueOf(500));

        return scooter;
    }
}