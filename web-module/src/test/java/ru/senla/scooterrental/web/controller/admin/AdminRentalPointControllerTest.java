package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.service.RentalPointService;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.CreateRentalPointRequest;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.RenameRentalPointRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

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

class AdminRentalPointControllerTest {

    private MockMvc mockMvc;
    private RentalPointService rentalPointService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rentalPointService = mock(RentalPointService.class);

        AdminRentalPointController controller =
                new AdminRentalPointController(rentalPointService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createRentalPoint_shouldCreateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        CreateRentalPointRequest request =
                new CreateRentalPointRequest("Central Point", 10L);

        when(rentalPointService.createRentalPoint("Central Point", 10L))
                .thenReturn(rentalPoint);

        mockMvc.perform(
                        post("/api/v1/admin/rental-points")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Central Point"))
                .andExpect(jsonPath("$.locationNodeId").value(10L))
                .andExpect(jsonPath("$.active").value(true));

        verify(rentalPointService).createRentalPoint("Central Point", 10L);
    }

    @Test
    void createRentalPoint_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        CreateRentalPointRequest request =
                new CreateRentalPointRequest("", 10L);

        mockMvc.perform(
                        post("/api/v1/admin/rental-points")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalPointService);
    }

    @Test
    void createRentalPoint_shouldReturnBadRequest_whenLocationNodeIdIsNull() throws Exception {
        CreateRentalPointRequest request =
                new CreateRentalPointRequest("Central Point", null);

        mockMvc.perform(
                        post("/api/v1/admin/rental-points")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalPointService);
    }

    @Test
    void getRentalPoints_shouldReturnAllSuccessfully() throws Exception {
        RentalPoint first = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );
        RentalPoint second = rentalPoint(
                2L,
                "North Point",
                20L,
                "North District",
                false
        );

        when(rentalPointService.findAllRentalPoints())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/admin/rental-points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationNodeId").value(10L))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].locationNodeId").value(20L))
                .andExpect(jsonPath("$[1].active").value(false));
        verify(rentalPointService).findAllRentalPoints();
    }

    @Test
    void getRentalPoints_shouldReturnActiveSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        when(rentalPointService.findActiveRentalPoints())
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(get("/api/v1/admin/rental-points")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Central Point"))
                .andExpect(jsonPath("$[0].locationNodeId").value(10L))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(rentalPointService).findActiveRentalPoints();
    }

    @Test
    void getRentalPointDetails_shouldReturnSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        when(rentalPointService.getRentalPointById(1L))
                .thenReturn(rentalPoint);
        when(rentalPointService.getRentalPointScooters(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/rental-points/1"))
                .andExpect(status().isOk());

        verify(rentalPointService).getRentalPointById(1L);
        verify(rentalPointService).getRentalPointScooters(1L);
    }

    @Test
    void getRentalPointsByLocation_shouldReturnAllSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        when(rentalPointService.findRentalPointsByLocationNode(10L))
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(get("/api/v1/admin/rental-points/by-location/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Central Point"))
                .andExpect(jsonPath("$[0].locationNodeId").value(10L))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(rentalPointService).findRentalPointsByLocationNode(10L);
    }

    @Test
    void getRentalPointsByLocation_shouldReturnActiveSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        when(rentalPointService.findActiveRentalPointsByLocationNode(10L))
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(
                        get("/api/v1/admin/rental-points/by-location/10")
                                .param("activeOnly", "true")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(rentalPointService).findActiveRentalPointsByLocationNode(10L);
    }

    @Test
    void renameRentalPoint_shouldRenameSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "New Point",
                10L,
                "Central District",
                true
        );

        RenameRentalPointRequest request =
                new RenameRentalPointRequest("New Point");

        when(rentalPointService.renameRentalPoint(1L, "New Point"))
                .thenReturn(rentalPoint);

        mockMvc.perform(
                        patch("/api/v1/admin/rental-points/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Point"))
                .andExpect(jsonPath("$.locationNodeId").value(10L))
                .andExpect(jsonPath("$.active").value(true));

        verify(rentalPointService).renameRentalPoint(1L, "New Point");
    }

    @Test
    void renameRentalPoint_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        RenameRentalPointRequest request =
                new RenameRentalPointRequest("");

        mockMvc.perform(
                        patch("/api/v1/admin/rental-points/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalPointService);
    }

    @Test
    void activateRentalPoint_shouldActivateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                true
        );

        when(rentalPointService.activateRentalPoint(1L))
                .thenReturn(rentalPoint);

        mockMvc.perform(patch("/api/v1/admin/rental-points/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(true));

        verify(rentalPointService).activateRentalPoint(1L);
    }

    @Test
    void deactivateRentalPoint_shouldDeactivateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint(
                1L,
                "Central Point",
                10L,
                "Central District",
                false
        );

        when(rentalPointService.deactivateRentalPoint(1L))
                .thenReturn(rentalPoint);

        mockMvc.perform(patch("/api/v1/admin/rental-points/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(false));

        verify(rentalPointService).deactivateRentalPoint(1L);
    }

    @Test
    void deleteRentalPoint_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/rental-points/1"))
                .andExpect(status().isNoContent());

        verify(rentalPointService).deleteRentalPoint(1L);
    }

    private RentalPoint rentalPoint(Long id,
                                    String name,
                                    Long locationNodeId,
                                    String locationNodeName,
                                    boolean active) {
        RentalPoint rentalPoint = mock(RentalPoint.class);
        LocationNode locationNode = mock(LocationNode.class);

        lenient().when(rentalPoint.getId()).thenReturn(id);
        lenient().when(rentalPoint.getName()).thenReturn(name);
        lenient().when(rentalPoint.getLocationNode()).thenReturn(locationNode);
        lenient().when(rentalPoint.isActive()).thenReturn(active);

        lenient().when(locationNode.getId()).thenReturn(locationNodeId);
        lenient().when(locationNode.getName()).thenReturn(locationNodeName);

        return rentalPoint;
    }
}