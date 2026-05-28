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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        User admin = admin();

        when(userService.registerAdmin(any(User.class)))
                .thenReturn(admin);

        mockMvc.perform(
                        post("/api/v1/admin/users/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("Adminov"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.verified").value(true));

        verify(userService).registerAdmin(any(User.class));
    }

    @Test
    void registerAdmin_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        RegisterUserRequest request = new RegisterUserRequest(
                "",
                "",
                "",
                "",
                ""
        );

        mockMvc.perform(
                        post("/api/v1/admin/users/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void registerAdmin_shouldReturnBadRequest_whenEmailIsInvalid()
            throws Exception {

        RegisterUserRequest request = new RegisterUserRequest(
                "wrong-email",
                "Password123",
                "Admin",
                "Adminov",
                "+79991234567"
        );

        mockMvc.perform(
                        post("/api/v1/admin/users/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void registerAdmin_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/users/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_shouldReturnAllUsers() throws Exception {
        User user = user();

        when(userService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("ivan@example.com"))
                .andExpect(jsonPath("$[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$[0].lastName").value("Ivanov"))
                .andExpect(jsonPath("$[0].phone").value("+79991234567"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].balance").value(0))
                .andExpect(jsonPath("$[0].verified").value(false));

        verify(userService).getAllUsers();
    }

    @Test
    void getUsers_shouldReturnEmptyListSuccessfully() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService).getAllUsers();
    }

    @Test
    void getUsers_shouldReturnUsersByRole() throws Exception {
        User user = user();

        when(userService.getUsersByRole(Role.USER))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].role").value("USER"));

        verify(userService).getUsersByRole(Role.USER);
    }

    @Test
    void getUsers_shouldReturnUsersByStatus() throws Exception {
        User user = user();

        when(userService.getUsersByStatus(UserStatus.ACTIVE))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(userService).getUsersByStatus(UserStatus.ACTIVE);
    }

    @Test
    void getUsers_shouldPreferRoleOverStatus() throws Exception {
        User user = user();

        when(userService.getUsersByRole(Role.USER))
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "USER")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("USER"));

        verify(userService).getUsersByRole(Role.USER);
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenRoleIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "WRONG_ROLE"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_shouldReturnBadRequest_whenStatusIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "WRONG_STATUS"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUserById_shouldReturnUserSuccessfully() throws Exception {
        User user = user();

        when(userService.getById(1L))
                .thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).getById(1L);
    }

    @Test
    void getUserById_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/v1/admin/users/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void verifyUser_shouldVerifySuccessfully() throws Exception {
        User user = verifiedUser();

        when(userService.verifyUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.verified").value(true));

        verify(userService).verifyUser(1L);
    }

    @Test
    void verifyUser_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(patch("/api/v1/admin/users/abc/verify"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void blockUser_shouldBlockSuccessfully() throws Exception {
        User user = blockedUser();

        when(userService.blockUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(userService).blockUser(1L);
    }

    @Test
    void blockUser_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(patch("/api/v1/admin/users/abc/block"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void activateUser_shouldActivateSuccessfully() throws Exception {
        User user = user();

        when(userService.activateUser(1L))
                .thenReturn(user);

        mockMvc.perform(patch("/api/v1/admin/users/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).activateUser(1L);
    }

    @Test
    void activateUser_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(patch("/api/v1/admin/users/abc/activate"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addBalance_shouldAddBalanceSuccessfully() throws Exception {
        User user = userWithBalance(BigDecimal.valueOf(500));

        BalanceRequest request =
                new BalanceRequest(BigDecimal.valueOf(500));

        when(userService.addBalance(1L, BigDecimal.valueOf(500)))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/admin/users/1/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.balance").value(500));

        verify(userService).addBalance(1L, BigDecimal.valueOf(500));
    }

    @Test
    void addBalance_shouldReturnBadRequest_whenRequestIsInvalid()
            throws Exception {

        BalanceRequest request =
                new BalanceRequest(BigDecimal.valueOf(-100));

        mockMvc.perform(
                        post("/api/v1/admin/users/1/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addBalance_shouldReturnBadRequest_whenBodyIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/users/1/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void addBalance_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        BalanceRequest request =
                new BalanceRequest(BigDecimal.valueOf(500));

        mockMvc.perform(
                        post("/api/v1/admin/users/abc/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void buyThreeDaySubscription_shouldBuySuccessfully() throws Exception {
        User user = userWithSubscription();

        when(userService.buyThreeDaySubscription(1L))
                .thenReturn(user);

        mockMvc.perform(
                        post("/api/v1/admin/users/1/subscription/three-days")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subscriptionPurchasedAt").exists())
                .andExpect(jsonPath("$.subscriptionExpiresAt").exists());

        verify(userService).buyThreeDaySubscription(1L);
    }

    @Test
    void buyThreeDaySubscription_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/admin/users/abc/subscription/three-days")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_shouldReturnBadRequest_whenUserIdTypeIsInvalid()
            throws Exception {

        mockMvc.perform(delete("/api/v1/admin/users/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    private User user() {
        return user(
                1L,
                "ivan@example.com",
                "Ivan",
                "Ivanov",
                "+79991234567",
                Role.USER,
                UserStatus.ACTIVE,
                BigDecimal.ZERO,
                false,
                null,
                null
        );
    }

    private User verifiedUser() {
        return user(
                1L,
                "ivan@example.com",
                "Ivan",
                "Ivanov",
                "+79991234567",
                Role.USER,
                UserStatus.ACTIVE,
                BigDecimal.ZERO,
                true,
                null,
                null
        );
    }

    private User blockedUser() {
        return user(
                1L,
                "ivan@example.com",
                "Ivan",
                "Ivanov",
                "+79991234567",
                Role.USER,
                UserStatus.BLOCKED,
                BigDecimal.ZERO,
                false,
                null,
                null
        );
    }

    private User userWithBalance(BigDecimal balance) {
        return user(
                1L,
                "ivan@example.com",
                "Ivan",
                "Ivanov",
                "+79991234567",
                Role.USER,
                UserStatus.ACTIVE,
                balance,
                false,
                null,
                null
        );
    }

    private User userWithSubscription() {
        return user(
                1L,
                "ivan@example.com",
                "Ivan",
                "Ivanov",
                "+79991234567",
                Role.USER,
                UserStatus.ACTIVE,
                BigDecimal.ZERO,
                false,
                LocalDateTime.of(2026, 5, 18, 10, 0),
                LocalDateTime.of(2026, 5, 21, 10, 0)
        );
    }

    private User admin() {
        return user(
                2L,
                "admin@example.com",
                "Admin",
                "Adminov",
                "+79991234567",
                Role.ADMIN,
                UserStatus.ACTIVE,
                BigDecimal.ZERO,
                true,
                null,
                null
        );
    }

    private User user(Long id,
                      String email,
                      String firstName,
                      String lastName,
                      String phone,
                      Role role,
                      UserStatus status,
                      BigDecimal balance,
                      boolean verified,
                      LocalDateTime subscriptionPurchasedAt,
                      LocalDateTime subscriptionExpiresAt) {
        User user = mock(User.class);

        lenient().when(user.getId()).thenReturn(id);
        lenient().when(user.getEmail()).thenReturn(email);
        lenient().when(user.getFirstName()).thenReturn(firstName);
        lenient().when(user.getLastName()).thenReturn(lastName);
        lenient().when(user.getPhone()).thenReturn(phone);
        lenient().when(user.getRole()).thenReturn(role);
        lenient().when(user.getStatus()).thenReturn(status);
        lenient().when(user.getBalance()).thenReturn(balance);
        lenient().when(user.isVerified()).thenReturn(verified);
        lenient().when(user.getSubscriptionPurchasedAt())
                .thenReturn(subscriptionPurchasedAt);
        lenient().when(user.getSubscriptionExpiresAt())
                .thenReturn(subscriptionExpiresAt);
        lenient().when(user.getCreatedAt())
                .thenReturn(LocalDateTime.of(2026, 5, 18, 10, 0));

        return user;
    }
}