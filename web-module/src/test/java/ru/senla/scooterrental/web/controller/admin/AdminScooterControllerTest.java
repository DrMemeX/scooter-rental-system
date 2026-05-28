package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.service.impl.FleetServiceImpl;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.CreateScooterRequest;
import ru.senla.scooterrental.web.dto.request.fleet.scooter.MoveScooterRequest;
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

class AdminScooterControllerTest {

    private MockMvc mockMvc;
    private FleetServiceImpl fleetService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetServiceImpl.class);

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
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        CreateScooterRequest request =
                new CreateScooterRequest(1L, 2L, 100.0);

        when(fleetService.createScooter(1L, 2L, 100.0))
                .thenReturn(scooter);

        mockMvc.perform(
                        post("/api/v1/admin/scooters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.currentCharge").value(100.0))
                .andExpect(jsonPath("$.totalMileageKm").value(0.0))
                .andExpect(jsonPath("$.modelId").value(1L))
                .andExpect(jsonPath("$.scooterClass").value("BASIC"))
                .andExpect(jsonPath("$.maxSpeedKmPerHour").value(20.0))
                .andExpect(jsonPath("$.consumptionPerKm").value(2.0))
                .andExpect(jsonPath("$.pricePerMinute").value(8))
                .andExpect(jsonPath("$.pricePerHour").value(400))
                .andExpect(jsonPath("$.batteryCapacity").value(1000))
                .andExpect(jsonPath("$.rentalPointId").value(2L))
                .andExpect(jsonPath("$.rentalPointName").value("Central Point"));

        verify(fleetService).createScooter(1L, 2L, 100.0);
    }

    @Test
    void getScooters_shouldReturnAllSuccessfully() throws Exception {
        Scooter first = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );
        Scooter second = scooter(
                2L,
                1L,
                2L,
                "Central Point",
                50.0,
                10.5,
                ScooterStatus.RENTED
        );

        when(fleetService.findAllScooters())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/admin/scooters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].currentCharge").value(100.0))
                .andExpect(jsonPath("$[0].totalMileageKm").value(0.0))
                .andExpect(jsonPath("$[0].modelId").value(1L))
                .andExpect(jsonPath("$[0].rentalPointId").value(2L))
                .andExpect(jsonPath("$[0].rentalPointName").value("Central Point"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("RENTED"))
                .andExpect(jsonPath("$[1].currentCharge").value(50.0))
                .andExpect(jsonPath("$[1].totalMileageKm").value(10.5))
                .andExpect(jsonPath("$[1].modelId").value(1L))
                .andExpect(jsonPath("$[1].rentalPointId").value(2L))
                .andExpect(jsonPath("$[1].rentalPointName").value("Central Point"));

        verify(fleetService).findAllScooters();
    }

    @Test
    void getScooters_shouldReturnAvailableSuccessfully() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters")
                        .param("availableOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(fleetService).findAvailableScooters();
    }

    @Test
    void getScooters_shouldReturnByStatusSuccessfully() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.MAINTENANCE
        );

        when(fleetService.findScootersByStatus(ScooterStatus.MAINTENANCE))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters")
                        .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("MAINTENANCE"));

        verify(fleetService).findScootersByStatus(ScooterStatus.MAINTENANCE);
    }

    @Test
    void getScooters_shouldReturnByRentalPointSuccessfully() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        when(fleetService.findScootersByRentalPoint(2L))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters")
                        .param("rentalPointId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].rentalPointId").value(2L))
                .andExpect(jsonPath("$[0].rentalPointName").value("Central Point"));

        verify(fleetService).findScootersByRentalPoint(2L);
    }

    @Test
    void getScooters_shouldPreferAvailableOnlyOverStatusAndRentalPoint()
            throws Exception {

        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        when(fleetService.findAvailableScooters())
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters")
                        .param("availableOnly", "true")
                        .param("status", "RENTED")
                        .param("rentalPointId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(fleetService).findAvailableScooters();
    }

    @Test
    void getScooters_shouldPreferStatusOverRentalPoint() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.RENTED
        );

        when(fleetService.findScootersByStatus(ScooterStatus.RENTED))
                .thenReturn(List.of(scooter));

        mockMvc.perform(get("/api/v1/admin/scooters")
                        .param("status", "RENTED")
                        .param("rentalPointId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RENTED"));

        verify(fleetService).findScootersByStatus(ScooterStatus.RENTED);
    }

    @Test
    void getScooterById_shouldReturnSuccessfully() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                2L,
                "Central Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        when(fleetService.getScooterById(1L))
                .thenReturn(scooter);

        mockMvc.perform(get("/api/v1/admin/scooters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.currentCharge").value(100.0))
                .andExpect(jsonPath("$.totalMileageKm").value(0.0))
                .andExpect(jsonPath("$.modelId").value(1L))
                .andExpect(jsonPath("$.rentalPointId").value(2L))
                .andExpect(jsonPath("$.rentalPointName").value("Central Point"));

        verify(fleetService).getScooterById(1L);
    }

    @Test
    void moveScooter_shouldMoveSuccessfully() throws Exception {
        Scooter scooter = scooter(
                1L,
                1L,
                3L,
                "North Point",
                100.0,
                0.0,
                ScooterStatus.AVAILABLE
        );

        MoveScooterRequest request =
                new MoveScooterRequest(3L);

        when(fleetService.moveScooterToRentalPoint(1L, 3L))
                .thenReturn(scooter);

        mockMvc.perform(
                        patch("/api/v1/admin/scooters/1/move")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.rentalPointId").value(3L))
                .andExpect(jsonPath("$.rentalPointName").value("North Point"));

        verify(fleetService).moveScooterToRentalPoint(1L, 3L);
    }

    @Test
    void moveScooter_shouldReturnBadRequest_whenRentalPointIdIsNull()
            throws Exception {

        MoveScooterRequest request =
                new MoveScooterRequest(null);

        mockMvc.perform(
                        patch("/api/v1/admin/scooters/1/move")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void deleteScooter_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/scooters/1"))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteScooter(1L);
    }

    private Scooter scooter(Long id,
                            Long modelId,
                            Long rentalPointId,
                            String rentalPointName,
                            Double currentCharge,
                            Double totalMileageKm,
                            ScooterStatus status) {
        Scooter scooter = mock(Scooter.class);
        ScooterModel model = mock(ScooterModel.class);
        RentalPoint rentalPoint = mock(RentalPoint.class);

        lenient().when(scooter.getId()).thenReturn(id);
        lenient().when(scooter.getModel()).thenReturn(model);
        lenient().when(scooter.getCurrentRentalPoint()).thenReturn(rentalPoint);
        lenient().when(scooter.getCurrentCharge()).thenReturn(currentCharge);
        lenient().when(scooter.getTotalMileageKm()).thenReturn(totalMileageKm);
        lenient().when(scooter.getStatus()).thenReturn(status);

        lenient().when(model.getId()).thenReturn(modelId);
        lenient().when(model.getScooterClass()).thenReturn(ScooterClass.BASIC);
        lenient().when(model.getMaxSpeedKmPerHour()).thenReturn(20.0);
        lenient().when(model.getConsumptionPerKm()).thenReturn(2.0);
        lenient().when(model.getPricePerMinute()).thenReturn(BigDecimal.valueOf(8));
        lenient().when(model.getPricePerHour()).thenReturn(BigDecimal.valueOf(400));
        lenient().when(model.getBatteryCapacity()).thenReturn(1000);

        lenient().when(rentalPoint.getId()).thenReturn(rentalPointId);
        lenient().when(rentalPoint.getName()).thenReturn(rentalPointName);

        return scooter;
    }
}