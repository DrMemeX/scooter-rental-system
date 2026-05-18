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
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.service.MaintenanceService;
import ru.senla.scooterrental.web.dto.request.maintenance.ChargeScooterRequest;
import ru.senla.scooterrental.web.dto.request.maintenance.MaintenanceEventRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMaintenanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = mock(MaintenanceService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminMaintenanceController(maintenanceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void reportTechnicalBreakdown_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.TECHNICAL_BREAKDOWN);
        MaintenanceEventRequest request = new MaintenanceEventRequest("Сломано колесо");

        when(maintenanceService.reportTechnicalBreakdown(1L, "Сломано колесо"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/technical-breakdown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TECHNICAL_BREAKDOWN"));

        verify(maintenanceService).reportTechnicalBreakdown(1L, "Сломано колесо");
    }

    @Test
    void reportTechnicalBreakdown_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/technical-breakdown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(maintenanceService);
    }

    @Test
    void reportTechnicalBreakdown_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        MaintenanceEventRequest request = new MaintenanceEventRequest("Сломано колесо");

        when(maintenanceService.reportTechnicalBreakdown(0L, "Сломано колесо"))
                .thenThrow(new MaintenanceValidationException(
                        "ID самоката должен быть положительным"
                ));

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/0/technical-breakdown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maintenanceService).reportTechnicalBreakdown(0L, "Сломано колесо");
    }

    @Test
    void reportUserDamage_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.USER_DAMAGE);
        MaintenanceEventRequest request = new MaintenanceEventRequest("Пользователь повредил руль");

        when(maintenanceService.reportUserDamage(1L, "Пользователь повредил руль"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/user-damage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("USER_DAMAGE"));

        verify(maintenanceService).reportUserDamage(1L, "Пользователь повредил руль");
    }

    @Test
    void reportUserDamage_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/user-damage")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(maintenanceService);
    }

    @Test
    void sendToMaintenance_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.SENT_TO_MAINTENANCE);
        MaintenanceEventRequest request = new MaintenanceEventRequest("Передан в сервисный центр");

        when(maintenanceService.sendToMaintenance(1L, "Передан в сервисный центр"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SENT_TO_MAINTENANCE"));

        verify(maintenanceService).sendToMaintenance(1L, "Передан в сервисный центр");
    }

    @Test
    void completeMaintenance_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.MAINTENANCE_COMPLETED);
        MaintenanceEventRequest request = new MaintenanceEventRequest("Техническое обслуживание завершено");

        when(maintenanceService.completeMaintenance(1L, "Техническое обслуживание завершено"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MAINTENANCE_COMPLETED"));

        verify(maintenanceService).completeMaintenance(1L, "Техническое обслуживание завершено");
    }

    @Test
    void chargeScooter_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.CHARGED);
        ChargeScooterRequest request = new ChargeScooterRequest(50.0, "Самокат заряжен");

        when(maintenanceService.chargeScooter(1L, 50.0, "Самокат заряжен"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CHARGED"));

        verify(maintenanceService).chargeScooter(1L, 50.0, "Самокат заряжен");
    }

    @Test
    void chargeScooter_shouldReturnBadRequest_whenAmountIsInvalid()
            throws Exception {

        ChargeScooterRequest request = new ChargeScooterRequest(-10.0, "Некорректная зарядка");

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(maintenanceService);
    }

    @Test
    void chargeScooter_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        ChargeScooterRequest request = new ChargeScooterRequest(50.0, "Некорректная зарядка");

        when(maintenanceService.chargeScooter(1L, 50.0, "Некорректная зарядка"))
                .thenThrow(new MaintenanceValidationException(
                        "Нельзя зарядить выше максимума"
                ));

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(maintenanceService).chargeScooter(1L, 50.0, "Некорректная зарядка");
    }

    @Test
    void markServiceRequired_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.SERVICE_REQUIRED);
        MaintenanceEventRequest request = new MaintenanceEventRequest("Проблема с аккумулятором");

        when(maintenanceService.markServiceRequired(1L, "Проблема с аккумулятором"))
                .thenReturn(event);

        mockMvc.perform(post("/api/v1/admin/maintenance/scooters/1/service-required")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SERVICE_REQUIRED"));

        verify(maintenanceService).markServiceRequired(1L, "Проблема с аккумулятором");
    }

    @Test
    void getEvents_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.CHARGED);

        when(maintenanceService.getAllEvents()).thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/admin/maintenance/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CHARGED"));

        verify(maintenanceService).getAllEvents();
    }

    @Test
    void getEventsByType_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.CHARGED);

        when(maintenanceService.getEventsByType(ServiceEventType.CHARGED))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/admin/maintenance/events")
                        .param("type", "CHARGED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CHARGED"));

        verify(maintenanceService).getEventsByType(ServiceEventType.CHARGED);
    }

    @Test
    void getEventsByType_shouldReturnBadRequest_whenTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/maintenance/events")
                        .param("type", "WRONG_TYPE"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(maintenanceService);
    }

    @Test
    void getEventsByScooterId_shouldReturnOk() throws Exception {
        ScooterServiceEvent event = event(ServiceEventType.CHARGED);

        when(maintenanceService.getEventsByScooterId(1L))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/admin/maintenance/scooters/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CHARGED"));

        verify(maintenanceService).getEventsByScooterId(1L);
    }

    @Test
    void getEventsByScooterId_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        when(maintenanceService.getEventsByScooterId(0L))
                .thenThrow(new MaintenanceValidationException(
                        "ID самоката должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/maintenance/scooters/0/events"))
                .andExpect(status().isBadRequest());

        verify(maintenanceService).getEventsByScooterId(0L);
    }

    private ScooterServiceEvent event(ServiceEventType type) {
        ScooterServiceEvent event = mock(ScooterServiceEvent.class);
        Scooter scooter = mock(Scooter.class);

        lenient().when(scooter.getId()).thenReturn(1L);

        lenient().when(event.getId()).thenReturn(1L);
        lenient().when(event.getScooter()).thenReturn(scooter);
        lenient().when(event.getType()).thenReturn(type);
        lenient().when(event.getDescription()).thenReturn("Сервисное событие");
        lenient().when(event.getCreatedAt()).thenReturn(LocalDateTime.now());

        return event;
    }
}