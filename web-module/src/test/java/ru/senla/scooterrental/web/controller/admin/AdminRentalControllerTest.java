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
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.service.impl.RentalServiceImpl;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.web.dto.request.rental.ApproveManualFinishRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
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

class AdminRentalControllerTest {

    private MockMvc mockMvc;
    private RentalServiceImpl rentalService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rentalService = mock(RentalServiceImpl.class);

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].tariffType").value("HOUR"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].distanceKm").value(0.2));

        verify(rentalService).getAllRentals();
    }

    @Test
    void getAllRentals_shouldReturnEmptyListSuccessfully() throws Exception {
        when(rentalService.getAllRentals())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(rentalService).getAllRentals();
    }

    @Test
    void getRentalById_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalOrThrow(1L))
                .thenReturn(rental);

        mockMvc.perform(get("/api/v1/admin/rentals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tariffType").value("HOUR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.distanceKm").value(0.2));

        verify(rentalService).getRentalOrThrow(1L);
    }

    @Test
    void getRentalById_shouldReturnNotFound_whenRentalDoesNotExist()
            throws Exception {

        when(rentalService.getRentalOrThrow(99L))
                .thenThrow(new RentalNotFoundException(
                        "Аренда с ID 99 не найдена"
                ));

        mockMvc.perform(get("/api/v1/admin/rentals/99"))
                .andExpect(status().isNotFound());

        verify(rentalService).getRentalOrThrow(99L);
    }

    @Test
    void getRentalById_shouldReturnBadRequest_whenRentalIdIsInvalid()
            throws Exception {

        when(rentalService.getRentalOrThrow(0L))
                .thenThrow(new RentalValidationException(
                        "ID аренды должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/rentals/0"))
                .andExpect(status().isBadRequest());

        verify(rentalService).getRentalOrThrow(0L);
    }

    @Test
    void getRentalById_shouldReturnBadRequest_whenRentalIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/rentals/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void getRentalsByUserId_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalsByUserId(1L))
                .thenReturn(List.of(rental));

        mockMvc.perform(get("/api/v1/admin/rentals/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(rentalService).getRentalsByUserId(1L);
    }

    @Test
    void getRentalsByUserId_shouldReturnBadRequest_whenUserIdIsInvalid()
            throws Exception {

        when(rentalService.getRentalsByUserId(0L))
                .thenThrow(new RentalValidationException(
                        "ID пользователя должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/rentals/users/0"))
                .andExpect(status().isBadRequest());

        verify(rentalService).getRentalsByUserId(0L);
    }

    @Test
    void getRentalsByUserId_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/rentals/users/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void getRentalsByScooterId_shouldReturnSuccessfully() throws Exception {
        Rental rental = rental();

        when(rentalService.getRentalsByScooterId(1L))
                .thenReturn(List.of(rental));

        mockMvc.perform(get("/api/v1/admin/rentals/scooters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(rentalService).getRentalsByScooterId(1L);
    }

    @Test
    void getRentalsByScooterId_shouldReturnBadRequest_whenScooterIdIsInvalid()
            throws Exception {

        when(rentalService.getRentalsByScooterId(0L))
                .thenThrow(new RentalValidationException(
                        "ID самоката должен быть положительным"
                ));

        mockMvc.perform(get("/api/v1/admin/rentals/scooters/0"))
                .andExpect(status().isBadRequest());

        verify(rentalService).getRentalsByScooterId(0L);
    }

    @Test
    void getRentalsByScooterId_shouldReturnBadRequest_whenScooterIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/rentals/scooters/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(rentalService).approveManualFinish(1L, 1L, 0.2, null);
    }

    @Test
    void approveManualFinish_shouldApproveSuccessfully_withPromoCode()
            throws Exception {

        Rental rental = rental();

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(1L, 0.2, "SALE10");

        when(rentalService.approveManualFinish(1L, 1L, 0.2, "SALE10"))
                .thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/admin/rentals/1/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(rentalService).approveManualFinish(1L, 1L, 0.2, "SALE10");
    }

    @Test
    void approveManualFinish_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(null, -1.0, null);

        mockMvc.perform(
                        post("/api/v1/admin/rentals/1/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void approveManualFinish_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/rentals/1/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void approveManualFinish_shouldReturnNotFound_whenRentalDoesNotExist()
            throws Exception {

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(1L, 0.2, null);

        when(rentalService.approveManualFinish(99L, 1L, 0.2, null))
                .thenThrow(new RentalNotFoundException(
                        "Аренда с ID 99 не найдена"
                ));

        mockMvc.perform(
                        post("/api/v1/admin/rentals/99/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(rentalService).approveManualFinish(99L, 1L, 0.2, null);
    }

    @Test
    void approveManualFinish_shouldReturnBadRequest_whenServiceThrowsValidation()
            throws Exception {

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(1L, 1000.0, null);

        when(rentalService.approveManualFinish(1L, 1L, 1000.0, null))
                .thenThrow(new RentalValidationException(
                        "Указанная дистанция невозможна"
                ));

        mockMvc.perform(
                        post("/api/v1/admin/rentals/1/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(rentalService).approveManualFinish(1L, 1L, 1000.0, null);
    }

    @Test
    void approveManualFinish_shouldReturnBadRequest_whenRentalIdTypeIsInvalid()
            throws Exception {

        ApproveManualFinishRequest request =
                new ApproveManualFinishRequest(1L, 0.2, null);

        mockMvc.perform(
                        post("/api/v1/admin/rentals/abc/approve-manual-finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
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