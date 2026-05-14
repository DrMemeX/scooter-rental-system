package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.service.MaintenanceService;
import ru.senla.scooterrental.web.dto.request.maintenance.ChargeScooterRequest;
import ru.senla.scooterrental.web.dto.request.maintenance.MaintenanceEventRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMaintenanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = mock(MaintenanceService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new AdminMaintenanceController(maintenanceService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void reportTechnicalBreakdown_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event();

        MaintenanceEventRequest request =
                new MaintenanceEventRequest("Broken wheel");

        when(maintenanceService.reportTechnicalBreakdown(
                1L,
                "Broken wheel"
        )).thenReturn(event);

        mockMvc.perform(
                        post("/api/v1/admin/maintenance/scooters/1/technical-breakdown")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void chargeScooter_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event();

        ChargeScooterRequest request =
                new ChargeScooterRequest(
                        50.0,
                        "Charging"
                );

        when(maintenanceService.chargeScooter(
                1L,
                50.0,
                "Charging"
        )).thenReturn(event);

        mockMvc.perform(
                        post("/api/v1/admin/maintenance/scooters/1/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void getEvents_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event();

        when(maintenanceService.getAllEvents())
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/admin/maintenance/events"))
                .andExpect(status().isOk());
    }

    @Test
    void getEventsByType_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event();

        when(maintenanceService.getEventsByType(ServiceEventType.CHARGED))
                .thenReturn(List.of(event));

        mockMvc.perform(
                        get("/api/v1/admin/maintenance/events")
                                .param("type", "CHARGED")
                )
                .andExpect(status().isOk());
    }

    private ScooterServiceEvent event() {
        ScooterServiceEvent event = mock(ScooterServiceEvent.class);
        Scooter scooter = mock(Scooter.class);

        lenient().when(scooter.getId()).thenReturn(1L);

        lenient().when(event.getId()).thenReturn(1L);
        lenient().when(event.getScooter()).thenReturn(scooter);
        lenient().when(event.getType())
                .thenReturn(ServiceEventType.CHARGED);
        lenient().when(event.getDescription()).thenReturn("Test");

        return event;
    }
}