package ru.senla.scooterrental.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.service.RentalService;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.rental.FinishRentalRequest;
import ru.senla.scooterrental.web.dto.request.rental.StartRentalRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserRentalControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RentalService rentalService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
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
        User user = user();
        Scooter scooter = scooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

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
                .andExpect(status().isCreated());

        verify(userService).getByEmail("ivan@example.com");
        verify(rentalService).startRental(1L, 10L, TariffType.HOUR, 2);
    }

    @Test
    void finishRental_shouldFinishRentalSuccessfully() throws Exception {
        User user = user();
        Scooter scooter = scooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);
        Rental finishedRental = new Rental(user, scooter, TariffType.HOUR, 2);

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
                .andExpect(status().isOk());

        verify(rentalService).getRentalOrThrow(100L);
        verify(rentalService).finishRental(100L, 5L, 0.2, null);
    }

    @Test
    void finishRental_shouldAllowAdminToFinishAnyRental() throws Exception {
        User rentalOwner = user();
        User admin = admin();
        Scooter scooter = scooter();
        Rental rental = new Rental(rentalOwner, scooter, TariffType.HOUR, 2);

        FinishRentalRequest request = new FinishRentalRequest(
                5L,
                0.2,
                null
        );

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        when(authentication.getName()).thenReturn("admin@example.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication)
                .getAuthorities();
        when(userService.getByEmail("admin@example.com")).thenReturn(admin);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.finishRental(100L, 5L, 0.2, null))
                .thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/finish")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void requestManualFinish_shouldRequestSuccessfully() throws Exception {
        User user = user();
        Scooter scooter = scooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalOrThrow(100L)).thenReturn(rental);
        when(rentalService.requestManualFinish(100L)).thenReturn(rental);

        mockMvc.perform(
                        post("/api/v1/rentals/100/manual-finish")
                                .principal(authentication)
                )
                .andExpect(status().isOk());

        verify(rentalService).requestManualFinish(100L);
    }

    @Test
    void getMyRentals_shouldReturnCurrentUserRentals() throws Exception {
        User user = user();
        Scooter scooter = scooter();
        Rental rental = new Rental(user, scooter, TariffType.HOUR, 2);

        when(authentication.getName()).thenReturn("ivan@example.com");
        when(userService.getByEmail("ivan@example.com")).thenReturn(user);
        when(rentalService.getRentalsByUserId(1L)).thenReturn(List.of(rental));

        mockMvc.perform(
                        get("/api/v1/rentals/my")
                                .principal(authentication)
                )
                .andExpect(status().isOk());

        verify(rentalService).getRentalsByUserId(1L);
    }

    private User user() {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("ivan@example.com");
        return user;
    }

    private User admin() {
        User admin = mock(User.class);
        lenient().when(admin.getId()).thenReturn(99L);
        lenient().when(admin.getEmail()).thenReturn("admin@example.com");
        return admin;
    }

    private Scooter scooter() {
        Scooter scooter = mock(Scooter.class);
        lenient().when(scooter.getId()).thenReturn(10L);
        return scooter;
    }
}