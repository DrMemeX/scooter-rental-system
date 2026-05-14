package ru.senla.scooterrental.web.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRentalPointControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FleetService fleetService;

    @BeforeEach
    void setUp() {
        UserRentalPointController controller =
                new UserRentalPointController(fleetService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getActiveRentalPoints_shouldReturnRentalPoints() throws Exception {
        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.findActiveRentalPoints())
                .thenReturn(List.of(rentalPoint));

        mockMvc.perform(get("/api/v1/rental-points"))
                .andExpect(status().isOk());

        verify(fleetService).findActiveRentalPoints();
    }

    @Test
    void getRentalPointDetails_shouldReturnDetailsSuccessfully()
            throws Exception {

        RentalPoint rentalPoint = rentalPoint();

        when(fleetService.getActiveRentalPointById(1L))
                .thenReturn(rentalPoint);

        when(fleetService.getRentalPointScooters(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/rental-points/1"))
                .andExpect(status().isOk());

        verify(fleetService).getActiveRentalPointById(1L);
        verify(fleetService).getRentalPointScooters(1L);
    }

    private RentalPoint rentalPoint() {
        RentalPoint rentalPoint = mock(RentalPoint.class);
        LocationNode location = mock(LocationNode.class);

        lenient().when(rentalPoint.getId()).thenReturn(1L);
        lenient().when(rentalPoint.getName()).thenReturn("Central Point");
        lenient().when(rentalPoint.getLocationNode()).thenReturn(location);
        lenient().when(rentalPoint.isActive()).thenReturn(true);

        lenient().when(location.getId()).thenReturn(10L);
        lenient().when(location.getName()).thenReturn("Central District");

        return rentalPoint;
    }
}