package ru.senla.scooterrental.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.user.BalanceRequest;
import ru.senla.scooterrental.web.dto.request.user.ChangeEmailRequest;
import ru.senla.scooterrental.web.dto.request.user.ChangePasswordRequest;
import ru.senla.scooterrental.web.dto.request.user.UpdateUserProfileRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserUserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private Authentication authentication;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authentication = mock(Authentication.class);

        UserUserController controller =
                new UserUserController(userService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() throws Exception {
        User user = user();

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.getById(1L))
                .thenReturn(user);

        mockMvc.perform(
                        get("/api/v1/users/1")
                                .principal(authentication)
                )
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldReturnForbidden_whenUserRequestsAnotherUser()
            throws Exception {

        User currentUser = user();

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        ))
                .when(authentication)
                .getAuthorities();

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(currentUser);

        mockMvc.perform(
                        get("/api/v1/users/999")
                                .principal(authentication)
                )
                .andExpect(status().isForbidden());

        verify(userService, never())
                .getById(999L);
    }

    @Test
    void getUserById_shouldAllowAdminToAccessAnotherUser()
            throws Exception {

        User currentUser = user();
        User targetUser = user();

        when(authentication.getName())
                .thenReturn("admin@example.com");

        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        ))
                .when(authentication)
                .getAuthorities();

        when(userService.getByEmail("admin@example.com"))
                .thenReturn(currentUser);

        when(userService.getById(999L))
                .thenReturn(targetUser);

        mockMvc.perform(
                        get("/api/v1/users/999")
                                .principal(authentication)
                )
                .andExpect(status().isOk());

        verify(userService)
                .getById(999L);
    }

    @Test
    void getUserById_shouldReturnBadRequest_whenIdInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/abc")
                                .principal(authentication)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateProfile_shouldUpdateSuccessfully() throws Exception {
        User user = user();

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        "Ivan",
                        "Petrov",
                        "+79999999999"
                );

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.updateProfile(
                1L,
                "Ivan",
                "Petrov",
                "+79999999999"
        )).thenReturn(user);

        mockMvc.perform(
                        patch("/api/v1/users/1/profile")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void changeEmail_shouldChangeEmailSuccessfully() throws Exception {
        User user = user();

        ChangeEmailRequest request =
                new ChangeEmailRequest("new@example.com");

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.changeEmail(1L, "new@example.com"))
                .thenReturn(user);

        mockMvc.perform(
                        patch("/api/v1/users/1/email")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void changeEmail_shouldReturnBadRequest_whenEmailInvalid()
            throws Exception {

        ChangeEmailRequest request =
                new ChangeEmailRequest("wrongEmail");

        mockMvc.perform(
                        patch("/api/v1/users/1/email")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully()
            throws Exception {

        User user = user();

        ChangePasswordRequest request =
                new ChangePasswordRequest("Password123");

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.changePassword(1L, "Password123"))
                .thenReturn(user);

        mockMvc.perform(
                        patch("/api/v1/users/1/password")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenPasswordInvalid()
            throws Exception {

        ChangePasswordRequest request =
                new ChangePasswordRequest("1");

        mockMvc.perform(
                        patch("/api/v1/users/1/password")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addBalance_shouldAddBalanceSuccessfully() throws Exception {
        User user = user();

        BalanceRequest request =
                new BalanceRequest(BigDecimal.valueOf(500));

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.addBalance(
                1L,
                BigDecimal.valueOf(500)
        )).thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/users/1/balance")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void addBalance_shouldReturnBadRequest_whenAmountNegative()
            throws Exception {

        BalanceRequest request =
                new BalanceRequest(
                        BigDecimal.valueOf(-500)
                );

        mockMvc.perform(
                        post("/api/v1/users/1/balance")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }


    @Test
    void buyThreeDaySubscription_shouldBuySuccessfully()
            throws Exception {

        User user = user();

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        when(userService.buyThreeDaySubscription(1L))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/users/1/subscription/three-days")
                                .principal(authentication)
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        User user = user();

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(user);

        mockMvc.perform(
                        delete("/api/v1/users/1")
                                .principal(authentication)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenUserDeletesAnotherUser()
            throws Exception {

        User currentUser = user();

        when(authentication.getName())
                .thenReturn("ivan@example.com");

        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        ))
                .when(authentication)
                .getAuthorities();

        when(userService.getByEmail("ivan@example.com"))
                .thenReturn(currentUser);

        mockMvc.perform(
                        delete("/api/v1/users/999")
                                .principal(authentication)
                )
                .andExpect(status().isForbidden());

        verify(userService, never())
                .deleteUser(999L);
    }

    private User user() {
        User user = mock(User.class);

        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail())
                .thenReturn("ivan@example.com");

        lenient().when(user.getRole())
                .thenReturn(Role.USER);

        return user;
    }
}