package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.location.CreateLocationRequest;
import ru.senla.scooterrental.web.dto.request.fleet.location.RenameLocationRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminLocationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
        fleetService = mock(FleetService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminLocationController(fleetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createLocation_shouldReturnCreated() throws Exception {
        LocationNode location = location();

        CreateLocationRequest request = new CreateLocationRequest(
                "Center",
                LocationType.RENTAL_POINT,
                null
        );

        when(fleetService.createLocation(
                "Center",
                LocationType.RENTAL_POINT,
                null
        )).thenReturn(location);

        mockMvc.perform(
                        post("/api/v1/admin/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }

    @Test
    void getLocations_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.findAllLocations())
                .thenReturn(List.of(location));

        mockMvc.perform(get("/api/v1/admin/locations"))
                .andExpect(status().isOk());
    }

    @Test
    void renameLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        RenameLocationRequest request =
                new RenameLocationRequest("New name");

        when(fleetService.renameLocation(1L, "New name"))
                .thenReturn(location);

        mockMvc.perform(
                        patch("/api/v1/admin/locations/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void activateLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.activateLocation(1L))
                .thenReturn(location);

        mockMvc.perform(patch("/api/v1/admin/locations/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.deactivateLocation(1L))
                .thenReturn(location);

        mockMvc.perform(patch("/api/v1/admin/locations/1/deactivate"))
                .andExpect(status().isOk());
    }

    private LocationNode location() {
        LocationNode location = mock(LocationNode.class);

        lenient().when(location.getId()).thenReturn(1L);
        lenient().when(location.getName()).thenReturn("Center");
        lenient().when(location.getType())
                .thenReturn(LocationType.RENTAL_POINT);
        lenient().when(location.isActive()).thenReturn(true);

        return location;
    }
}