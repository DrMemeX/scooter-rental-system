package ru.senla.scooterrental.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.rental.FinishRentalRequest;
import ru.senla.scooterrental.web.dto.request.rental.StartRentalRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRentalControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RentalService rentalService;
    private UserService userService;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        rentalService = mock(RentalService.class);
        userService = mock(UserService.class);
        authentication = mock(Authentication.class);

        UserRentalController controller = new UserRentalController(
                rentalService,
                userService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void startRental_shouldStartRentalSuccessfully() throws Exception {
        User user = user(1L, "ivan@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );

        StartRentalRequest request = new StartRentalRequest(
                1L,
                10L,
                TariffType.HOUR,
                2
        );

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.startRental(1L, 10L, TariffType.HOUR, 2))
                .thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/rentals/start")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.scooterId").value(10L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tariffType").value("HOUR"))
                .andExpect(jsonPath("$.plannedHours").value(2))
                .andExpect(jsonPath("$.maxAllowedMinutes").value(120))
                .andExpect(jsonPath("$.totalCost").value(800))
                .andExpect(jsonPath("$.distanceKm").value(0.0));

        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).startRental(1L, 10L, TariffType.HOUR, 2);
    }

    @Test
    void startRental_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        StartRentalRequest request = new StartRentalRequest(
                1L,
                null,
                null,
                -1
        );

        mockMvc.perform(
                        post("/api/v1/rentals/start")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void startRental_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/rentals/start")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void finishRental_shouldFinishRentalSuccessfully_forOwner()
            throws Exception {

        User user = user(1L, "ivan@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );
        Rental finishedRental = rental(
                100L,
                1L,
                10L,
                RentalStatus.FINISHED,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.2
        );

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                null
        );

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.finishRental(100L, 5L, 0.2, null))
                .thenReturn(finishedRental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.scooterId").value(10L))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.distanceKm").value(0.2));

        verify(rentalService).getRentalOrThrow(100L);
        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).finishRental(100L, 5L, 0.2, null);
    }

    @Test
    void finishRental_shouldFinishRentalSuccessfully_withPromoCode()
            throws Exception {

        User user = user(1L, "ivan@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );
        Rental finishedRental = rental(
                100L,
                1L,
                10L,
                RentalStatus.FINISHED,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(680),
                0.2
        );

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                "SALE10"
        );

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.finishRental(100L, 5L, 0.2, "SALE10"))
                .thenReturn(finishedRental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.totalCost").value(680));

        verify(rentalService).finishRental(100L, 5L, 0.2, "SALE10");
    }

    @Test
    void finishRental_shouldAllowAdminToFinishAnyRental()
            throws Exception {

        User rentalOwner = user(1L, "ivan@example.com");
        User admin = user(99L, "admin@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );
        Rental finishedRental = rental(
                100L,
                1L,
                10L,
                RentalStatus.FINISHED,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.2
        );

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                null
        );

        when(rental.getUser()).thenReturn(rentalOwner);
        when(authentication.getName()).thenReturn("admin@example.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication)
                .getAuthorities();
        when(userService.getByEmail("admin@example.com")).thenReturn(admin);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.finishRental(100L, 5L, 0.2, null))
                .thenReturn(finishedRental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));

        verify(rentalService).finishRental(100L, 5L, 0.2, null);
    }

    @Test
    void finishRental_shouldReturnForbidden_whenUserIsNotOwner()
            throws Exception {

        User rentalOwner = user(1L, "ivan@example.com");
        User anotherUser = user(2L, "petr@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                null
        );

        when(rental.getUser()).thenReturn(rentalOwner);
        when(authentication.getName()).thenReturn("petr@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("petr@example.com")).thenReturn(anotherUser);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        verify(rentalService).getRentalOrThrow(100L);
        verify(userService).getByEmail("petr@example.com");
        verify(rentalService, never()).finishRental(100L, 5L, 0.2, null);
    }

    @Test
    void finishRental_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        FinishRentalRequest request = new FinishRentalRequest(
                null,
                -1.0,
                null
        );

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void finishRental_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void finishRental_shouldReturnBadRequest_whenRentalIdTypeIsInvalid()
            throws Exception {

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                null
        );

        mockMvc.perform(
                        post("/api/v1/rentals/abc/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void requestManualFinish_shouldRequestSuccessfully_forOwner()
            throws Exception {

        User user = user(1L, "ivan@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );
        Rental updatedRental = rental(
                100L,
                1L,
                10L,
                RentalStatus.PENDING_MANAGER_CONFIRMATION,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.requestManualFinish(100L)).thenReturn(updatedRental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/manual-finish")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("PENDING_MANAGER_CONFIRMATION"));

        verify(rentalService).getRentalOrThrow(100L);
        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).requestManualFinish(100L);
    }

    @Test
    void requestManualFinish_shouldAllowAdminToRequestForAnyRental()
            throws Exception {

        User admin = user(99L, "admin@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );
        Rental updatedRental = rental(
                100L,
                1L,
                10L,
                RentalStatus.PENDING_MANAGER_CONFIRMATION,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );

        when(authentication.getName()).thenReturn("admin@example.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication)
                .getAuthorities();
        when(userService.getByEmail("admin@example.com")).thenReturn(admin);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.requestManualFinish(100L)).thenReturn(updatedRental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/manual-finish")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_MANAGER_CONFIRMATION"));

        verify(rentalService).requestManualFinish(100L);
    }

    @Test
    void requestManualFinish_shouldReturnForbidden_whenUserIsNotOwner()
            throws Exception {

        User anotherUser = user(2L, "petr@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.0
        );

        when(authentication.getName()).thenReturn("petr@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("petr@example.com")).thenReturn(anotherUser);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/manual-finish")
                                .principal(authentication)
                )
                .andExpect(status().isForbidden());

        verify(rentalService).getRentalOrThrow(100L);
        verify(userService).getByEmail("petr@example.com");
        verify(rentalService, never()).requestManualFinish(100L);
    }

    @Test
    void requestManualFinish_shouldReturnBadRequest_whenRentalIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/rentals/abc/manual-finish")
                                .principal(authentication)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(rentalService);
    }

    @Test
    void getMyRentals_shouldReturnCurrentUserRentals() throws Exception {
        User user = user(1L, "ivan@example.com");
        Rental rental = rental(
                100L,
                1L,
                10L,
                RentalStatus.ACTIVE,
                TariffType.HOUR,
                2,
                120,
                BigDecimal.valueOf(800),
                0.2
        );

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalsByUserId(1L))
                .thenReturn(List.of(rental));

        mockMvc.perform(
                        get("/api/v1/rentals/my")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].scooterId").value(10L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).getRentalsByUserId(1L);
    }

    @Test
    void getMyRentals_shouldReturnEmptyListSuccessfully() throws Exception {
        User user = user(1L, "ivan@example.com");

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalsByUserId(1L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/rentals/my")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).getRentalsByUserId(1L);
    }

    private User user(Long id, String email) {
        User user = mock(User.class);

        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getEmail()).thenReturn(email);

        return user;
    }

    private Rental rental(Long id,
                          Long userId,
                          Long scooterId,
                          RentalStatus status,
                          TariffType tariffType,
                          Integer plannedHours,
                          Integer maxAllowedMinutes,
                          BigDecimal totalCost,
                          Double distanceKm) {
        Rental rental = mock(Rental.class);
        User user = user(userId, "user" + userId + "@example.com");

        lenient().when(rental.getId()).thenReturn(id);
        lenient().when(rental.getUser()).thenReturn(user);
        lenient().when(rental.getUserId()).thenReturn(userId);
        lenient().when(rental.getScooterId()).thenReturn(scooterId);
        lenient().when(rental.getStatus()).thenReturn(status);
        lenient().when(rental.getStartTime())
                .thenReturn(LocalDateTime.of(2026, 5, 18, 10, 0));
        lenient().when(rental.getEndTime()).thenReturn(null);
        lenient().when(rental.getTariffType()).thenReturn(tariffType);
        lenient().when(rental.getPlannedHours()).thenReturn(plannedHours);
        lenient().when(rental.getMaxAllowedMinutes()).thenReturn(maxAllowedMinutes);
        lenient().when(rental.getTotalCost()).thenReturn(totalCost);
        lenient().when(rental.getPromoCodeId()).thenReturn(null);
        lenient().when(rental.getDiscountAmount()).thenReturn(BigDecimal.ZERO);
        lenient().when(rental.getTerminationReason()).thenReturn(null);
        lenient().when(rental.getDistanceKm()).thenReturn(distanceKm);

        return rental;
    }
}