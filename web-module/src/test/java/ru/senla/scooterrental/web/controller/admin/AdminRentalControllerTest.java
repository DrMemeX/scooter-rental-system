package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.web.dto.request.rental.ApproveManualFinishRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminRentalControllerTest {

    private MockMvc mockMvc;
    private RentalService rentalService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rentalService = mock(RentalService.class);

        AdminRentalController controller =
                new AdminRentalController(rentalService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllRentals_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getAllRentals())
                .thenReturn(List.of(rental));

        mockMvc.perform(get("/api/v1/admin/rentals"))
                .andExpect(status().isOk());

        verify(rentalService).getAllRentals();
    }

    @Test
    void getRentalById_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalOrThrow(1L))
                .thenReturn(rental);

        mockMvc.perform(get("/api/v1/admin/rentals/1"))
                .andExpect(status().isOk());

        verify(rentalService).getRentalOrThrow(1L);
    }

    @Test
    void getRentalsByUserId_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalsByUserId(1L))
                .thenReturn(List.of(rental));

        mockMvc.perform(get("/api/v1/admin/rentals/users/1"))
                .andExpect(status().isOk());

        verify(rentalService).getRentalsByUserId(1L);
    }

    @Test
    void getRentalsByScooterId_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalsByScooterId(1L))
                .thenReturn(List.of(rental));

        mockMvc.perform(get("/api/v1/admin/rentals/scooters/1"))
                .andExpect(status().isOk());

        verify(rentalService).getRentalsByScooterId(1L);
    }

    @Test
    void approveManualFinish_shouldApproveSuccessfully() throws Exception {
        Rental rental = rental();

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(1L, 0.2, null);

        when(rentalService.approveManualFinish(1L, 1L, 0.2, null))
                .thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/admin/rentals/1/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(rentalService).approveManualFinish(1L, 1L, 0.2, null);
    }

    private Rental rental() {
        Rental rental = mock(Rental.class);
        User user = mock(User.class);
        Scooter scooter = mock(Scooter.class);

        lenient().when(rental.getId()).thenReturn(1L);
        lenient().when(rental.getUser()).thenReturn(user);
        lenient().when(rental.getScooter()).thenReturn(scooter);
        lenient().when(rental.getTariffType()).thenReturn(TariffType.HOUR);
        lenient().when(rental.getStatus()).thenReturn(RentalStatus.ACTIVE);
        lenient().when(rental.getTotalCost()).thenReturn(BigDecimal.ZERO);
        lenient().when(rental.getDistanceKm()).thenReturn(0.2);

        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(scooter.getId()).thenReturn(1L);

        return rental;
    }
}