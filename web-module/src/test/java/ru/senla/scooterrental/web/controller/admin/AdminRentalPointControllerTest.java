package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.CreateRentalPointRequest;
import ru.senla.scooterrental.web.dto.request.fleet.rentalpoint.RenameRentalPointRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

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

class AdminRentalPointControllerTest {

    private MockMvc mockMvc;
    private FleetService fleetService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        AdminRentalPointController controller =
                new AdminRentalPointController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createRentalPoint_shouldCreateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        CreateRentalPointRequest request =
                new CreateRentalPointRequest("Central Point", 1L);

        when(fleetService.createRentalPoint("Central Point", 1L))
                .thenReturn(rentalPoint);

        mockMvc.perform(
                        post("/api/v1/admin/rental-points")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(fleetService).createRentalPoint("Central Point", 1L);
    }

    @Test
    void getRentalPoints_shouldReturnAllSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.findAllRentalPoints())
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(get("/api/v1/admin/rental-points"))
                .andExpect(status().isOk());

        verify(fleetService).findAllRentalPoints();
    }

    @Test
    void getRentalPoints_shouldReturnActiveSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.findActiveRentalPoints())
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(get("/api/v1/admin/rental-points?activeOnly=true"))
                .andExpect(status().isOk());

        verify(fleetService).findActiveRentalPoints();
    }

    @Test
    void getRentalPointDetails_shouldReturnSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.getRentalPointById(1L))
                .thenReturn(rentalPoint);
        when(fleetService.getRentalPointScooters(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/rental-points/1"))
                .andExpect(status().isOk());

        verify(fleetService).getRentalPointById(1L);
        verify(fleetService).getRentalPointScooters(1L);
    }

    @Test
    void renameRentalPoint_shouldRenameSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        RenameRentalPointRequest request =
                new RenameRentalPointRequest("New Point");

        when(fleetService.renameRentalPoint(1L, "New Point"))
                .thenReturn(rentalPoint);

        mockMvc.perform(
                        patch("/api/v1/admin/rental-points/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(fleetService).renameRentalPoint(1L, "New Point");
    }

    @Test
    void activateRentalPoint_shouldActivateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.activateRentalPoint(1L))
                .thenReturn(rentalPoint);

        mockMvc.perform(patch("/api/v1/admin/rental-points/1/activate"))
                .andExpect(status().isOk());

        verify(fleetService).activateRentalPoint(1L);
    }

    @Test
    void deactivateRentalPoint_shouldDeactivateSuccessfully() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.deactivateRentalPoint(1L))
                .thenReturn(rentalPoint);

        mockMvc.perform(patch("/api/v1/admin/rental-points/1/deactivate"))
                .andExpect(status().isOk());

        verify(fleetService).deactivateRentalPoint(1L);
    }

    @Test
    void deleteRentalPoint_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/rental-points/1"))
                .andExpect(status().isNoContent());

        verify(fleetService).deleteRentalPoint(1L);
    }

    private RentalPoint rentalPoint() {
        RentalPoint rentalPoint = mock(RentalPoint.class);
        LocationNode locationNode = mock(LocationNode.class);

        lenient().when(rentalPoint.getId()).thenReturn(1L);
        lenient().when(rentalPoint.getName()).thenReturn("Central Point");
        lenient().when(rentalPoint.getLocationNode()).thenReturn(locationNode);
        lenient().when(rentalPoint.isActive()).thenReturn(true);

        lenient().when(locationNode.getId()).thenReturn(1L);
        lenient().when(locationNode.getName()).thenReturn("Central District");

        return rentalPoint;
    }
}