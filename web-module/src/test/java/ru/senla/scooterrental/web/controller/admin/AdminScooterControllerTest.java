package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.CreateScooterRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.MoveScooterRequest;
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

class AdminScooterControllerTest {

    private MockMvc mockMvc;
    private FleetService fleetService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        AdminScooterController controller =
                new AdminScooterController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createScooter_shouldCreateSuccessfully() throws Exception {
        Scooter scooter = scooter();

        CreateScooterRequest request =
                new CreateScooterRequest(1L, 1L, 100.0);

        when(fleetService.createScooter(1L, 1L, 100.0))
                .thenReturn(scooter);

        mockMvc.perform(
                        post("/api/v1/admin/scooters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void getScooters_shouldReturnAllSuccessfully() throws Exception {
        Scooter scooter = scooter();

        when(fleetService.findAllScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters"))
                .andExpect(status().isOk());

        verify(fleetService).findAllScooters();
    }

    @Test
    void getScooters_shouldReturnAvailableSuccessfully()
            throws Exception {

        Scooter scooter = scooter();

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters?availableOnly=true"))
                .andExpect(status().isOk());

        verify(fleetService).findAvailableScooters();
    }

    @Test
    void getScooters_shouldReturnByStatusSuccessfully()
            throws Exception {

        Scooter scooter = scooter();

        when(fleetService.findScootersByStatus(ScooterStatus.AVAILABLE))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters?status=AVAILABLE"))
                .andExpect(status().isOk());

        verify(fleetService).findScootersByStatus(ScooterStatus.AVAILABLE);
    }

    @Test
    void getScooters_shouldReturnByRentalPointSuccessfully()
            throws Exception {

        Scooter scooter = scooter();

        when(fleetService.findScootersByRentalPoint(1L))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters?rentalPointId=1"))
                .andExpect(status().isOk());

        verify(fleetService).findScootersByRentalPoint(1L);
    }

    @Test
    void getScooterById_shouldReturnSuccessfully() throws Exception {
        Scooter scooter = scooter();

        when(fleetService.getScooterById(1L))
                .thenReturn(scooter);

        mockMvc.perform(get("/api/v1/admin/scooters/1"))
                .andExpect(status().isOk());

        verify(fleetService).getScooterById(1L);
    }

    @Test
    void moveScooter_shouldMoveSuccessfully() throws Exception {
        Scooter scooter = scooter();

        MoveScooterRequest request = new MoveScooterRequest(2L);

        when(fleetService.moveScooterToRentalPoint(1L, 2L))
                .thenReturn(scooter);

        mockMvc.perform(
                        patch("/api/v1/admin/scooters/1/move")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteScooter_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/scooters/1"))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteScooter(1L);
    }

    private Scooter scooter() {
        Scooter scooter = mock(Scooter.class);
        ScooterModel model = mock(ScooterModel.class);

        lenient().when(scooter.getId()).thenReturn(1L);
        lenient().when(scooter.getModel()).thenReturn(model);
        lenient().when(scooter.getCurrentCharge()).thenReturn(100.0);
        lenient().when(scooter.getStatus())
                .thenReturn(ScooterStatus.AVAILABLE);

        lenient().when(model.getId()).thenReturn(1L);
        lenient().when(model.getScooterClass())
                .thenReturn(ScooterClass.BASIC);
        lenient().when(model.getPricePerMinute())
                .thenReturn(BigDecimal.valueOf(8));
        lenient().when(model.getPricePerHour())
                .thenReturn(BigDecimal.valueOf(400));

        return scooter;
    }
}