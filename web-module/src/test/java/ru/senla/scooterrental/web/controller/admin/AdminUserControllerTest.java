package ru.senla.scooterrental.web.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.user.BalanceRequest;
import ru.senla.scooterrental.web.dto.request.user.RegisterUserRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;

import java.math.BigDecimal;
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

class AdminUserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        AdminUserController controller =
                new AdminUserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void registerAdmin_shouldRegisterSuccessfully() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "admin@example.com",
                "Password123",
                "Admin",
                "Adminov",
                "+79991234567"
        );

        User user = admin();

        when(userService.registerManager(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/admin/users/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());

        verify(userService).registerManager(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void getUsers_shouldReturnAllUsers() throws Exception {
        User user = user();

        when(userService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());

        verify(userService).getAllUsers();
    }

    @Test
    void getUsers_shouldReturnUsersByRole() throws Exception {
        User user = user();

        when(userService.getUsersByRole(Role.USER))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users?role=USER"))
                .andExpect(status().isOk());

        verify(userService).getUsersByRole(Role.USER);
    }

    @Test
    void getUsers_shouldReturnUsersByStatus() throws Exception {
        User user = user();

        when(userService.getUsersByStatus(UserStatus.ACTIVE))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users?status=ACTIVE"))
                .andExpect(status().isOk());

        verify(userService).getUsersByStatus(UserStatus.ACTIVE);
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() throws Exception {
        User user = user();

        when(userService.getById(1L))
                .thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk());

        verify(userService).getById(1L);
    }

    @Test
    void verifyUser_shouldVerifySuccessfully() throws Exception {
        User user = user();

        when(userService.verifyUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/verify"))
                .andExpect(status().isOk());

        verify(userService).verifyUser(1L);
    }

    @Test
    void blockUser_shouldBlockSuccessfully() throws Exception {
        User user = user();

        when(userService.blockUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/block"))
                .andExpect(status().isOk());

        verify(userService).blockUser(1L);
    }

    @Test
    void activateUser_shouldActivateSuccessfully() throws Exception {
        User user = user();

        when(userService.activateUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/activate"))
                .andExpect(status().isOk());

        verify(userService).activateUser(1L);
    }

    @Test
    void addBalance_shouldAddBalanceSuccessfully() throws Exception {
        User user = user();

        BalanceRequest request =
                new BalanceRequest(BigDecimal.valueOf(500));

        when(userService.addBalance(1L, BigDecimal.valueOf(500)))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/admin/users/1/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(userService).addBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    void buyThreeDaySubscription_shouldBuySuccessfully() throws Exception {
        User user = user();

        when(userService.buyThreeDaySubscription(1L))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/admin/users/1/subscription/three-days")
                )
                .andExpect(status().isOk());

        verify(userService).buyThreeDaySubscription(1L);
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    private User user() {
        User user = mock(User.class);

        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("ivan@example.com");
        lenient().when(user.getFirstName()).thenReturn("Ivan");
        lenient().when(user.getLastName()).thenReturn("Ivanov");
        lenient().when(user.getPhone()).thenReturn("+79991234567");
        lenient().when(user.getRole()).thenReturn(Role.USER);
        lenient().when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        lenient().when(user.getBalance()).thenReturn(BigDecimal.ZERO);
        lenient().when(user.isVerified()).thenReturn(false);

        return user;
    }

    private User admin() {
        User admin = mock(User.class);

        lenient().when(admin.getId()).thenReturn(2L);
        lenient().when(admin.getEmail()).thenReturn("admin@example.com");
        lenient().when(admin.getFirstName()).thenReturn("Admin");
        lenient().when(admin.getLastName()).thenReturn("Adminov");
        lenient().when(admin.getPhone()).thenReturn("+79991234567");
        lenient().when(admin.getRole()).thenReturn(Role.ADMIN);
        lenient().when(admin.getStatus()).thenReturn(UserStatus.ACTIVE);
        lenient().when(admin.getBalance()).thenReturn(BigDecimal.ZERO);
        lenient().when(admin.isVerified()).thenReturn(true);

        return admin;
    }
}