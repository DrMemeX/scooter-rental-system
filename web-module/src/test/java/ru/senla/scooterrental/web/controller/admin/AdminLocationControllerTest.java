package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.dto.request.fleet.location.CreateLocationRequest;
import ru.senla.scooterrental.web.dto.request.fleet.location.RenameLocationRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Center"))
                .andExpect(jsonPath("$.type").value("RENTAL_POINT"))
                .andExpect(jsonPath("$.active").value(true));

        verify(fleetService).createLocation(
                "Center",
                LocationType.RENTAL_POINT,
                null
        );
    }

    @Test
    void createLocation_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        CreateLocationRequest request = new CreateLocationRequest(
                "",
                null,
                null
        );

        mockMvc.perform(
                        post("/api/v1/admin/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void createLocation_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void createLocation_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        CreateLocationRequest request = new CreateLocationRequest(
                "District",
                LocationType.RENTAL_POINT,
                1L
        );

        when(fleetService.createLocation(
                "District",
                LocationType.RENTAL_POINT,
                1L
        )).thenThrow(new FleetValidationException(
                "Некорректная иерархия локаций"
        ));

        mockMvc.perform(
                        post("/api/v1/admin/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(fleetService).createLocation(
                "District",
                LocationType.RENTAL_POINT,
                1L
        );
    }

    @Test
    void createLocation_shouldReturnNotFound_whenParentLocationNotFound()
            throws Exception {

        CreateLocationRequest request = new CreateLocationRequest(
                "District",
                LocationType.DISTRICT,
                99L
        );

        when(fleetService.createLocation(
                "District",
                LocationType.DISTRICT,
                99L
        )).thenThrow(new FleetEntityNotFoundException(
                "Локация с ID 99 не найдена"
        ));

        mockMvc.perform(
                        post("/api/v1/admin/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(fleetService).createLocation(
                "District",
                LocationType.DISTRICT,
                99L
        );
    }

    @Test
    void getLocations_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.findAllLocations())
                .thenReturn(List.of(location));

        mockMvc.perform(get("/api/v1/admin/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Center"))
                .andExpect(jsonPath("$[0].type").value("RENTAL_POINT"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(fleetService).findAllLocations();
    }

    @Test
    void getLocations_shouldReturnOk_whenTypeProvided() throws Exception {
        LocationNode location = location();

        when(fleetService.findLocationsByType(LocationType.RENTAL_POINT))
                .thenReturn(List.of(location));

        mockMvc.perform(
                        get("/api/v1/admin/locations")
                                .param("type", "RENTAL_POINT")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("RENTAL_POINT"));

        verify(fleetService).findLocationsByType(LocationType.RENTAL_POINT);
    }

    @Test
    void getLocations_shouldReturnBadRequest_whenTypeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/locations")
                                .param("type", "WRONG_TYPE")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void renameLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        RenameLocationRequest request =
                new RenameLocationRequest("Новое название");

        when(fleetService.renameLocation(1L, "Новое название"))
                .thenReturn(location);

        mockMvc.perform(
                        patch("/api/v1/admin/locations/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Center"));

        verify(fleetService).renameLocation(1L, "Новое название");
    }

    @Test
    void renameLocation_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        RenameLocationRequest request =
                new RenameLocationRequest("");

        mockMvc.perform(
                        patch("/api/v1/admin/locations/1/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fleetService);
    }

    @Test
    void renameLocation_shouldReturnNotFound_whenLocationNotFound()
            throws Exception {

        RenameLocationRequest request =
                new RenameLocationRequest("Новое название");

        when(fleetService.renameLocation(99L, "Новое название"))
                .thenThrow(new FleetEntityNotFoundException(
                        "Локация с ID 99 не найдена"
                ));

        mockMvc.perform(
                        patch("/api/v1/admin/locations/99/rename")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(fleetService).renameLocation(99L, "Новое название");
    }

    @Test
    void activateLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.activateLocation(1L))
                .thenReturn(location);

        mockMvc.perform(patch("/api/v1/admin/locations/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(true));

        verify(fleetService).activateLocation(1L);
    }

    @Test
    void activateLocation_shouldReturnNotFound_whenLocationNotFound()
            throws Exception {

        when(fleetService.activateLocation(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Локация с ID 99 не найдена"
                ));

        mockMvc.perform(patch("/api/v1/admin/locations/99/activate"))
                .andExpect(status().isNotFound());

        verify(fleetService).activateLocation(99L);
    }

    @Test
    void deactivateLocation_shouldReturnOk() throws Exception {
        LocationNode location = location();

        when(fleetService.deactivateLocation(1L))
                .thenReturn(location);

        mockMvc.perform(patch("/api/v1/admin/locations/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(fleetService).deactivateLocation(1L);
    }

    @Test
    void deactivateLocation_shouldReturnNotFound_whenLocationNotFound()
            throws Exception {

        when(fleetService.deactivateLocation(99L))
                .thenThrow(new FleetEntityNotFoundException(
                        "Локация с ID 99 не найдена"
                ));

        mockMvc.perform(patch("/api/v1/admin/locations/99/deactivate"))
                .andExpect(status().isNotFound());

        verify(fleetService).deactivateLocation(99L);
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