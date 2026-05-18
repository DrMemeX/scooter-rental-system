package ru.senla.scooterrental.web.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.exceptions.UserAlreadyExistsException;
import ru.senla.scooterrental.user.repository.UserRepository;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.auth.LoginRequest;
import ru.senla.scooterrental.web.dto.request.auth.RegisterRequest;
import ru.senla.scooterrental.web.error.GlobalExceptionHandler;
import ru.senla.scooterrental.web.security.JwtService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(
                authenticationManager,
                jwtService,
                userRepository,
                userService,
                passwordEncoder
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void register_shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "ivan@example.com",
                "Password123",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        User user = new User(
                "ivan@example.com",
                "Password1234",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        user.assignUserRole();

        when(passwordEncoder.encode("Password123"))
                .thenReturn("Password1234");

        when(userService.registerUser(any(User.class)))
                .thenReturn(user);

        when(jwtService.generateToken(any(), any()))
                .thenReturn("jwt-token");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(passwordEncoder).encode("Password123");
        verify(userService).registerUser(any(User.class));
        verify(jwtService).generateToken(
                "ivan@example.com",
                Role.USER.name()
        );
    }

    @Test
    void register_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "",
                "",
                "",
                "",
                ""
        );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordEncoder, userService, jwtService);
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "Password123",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordEncoder, userService, jwtService);
    }

    @Test
    void register_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "ivan@example.com",
                "Password123",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("Password1234");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException(
                        "Пользователь с таким email уже существует."
                ));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(passwordEncoder).encode("Password123");
        verify(userService).registerUser(any(User.class));
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void register_shouldReturnConflict_whenPhoneAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "ivan@example.com",
                "Password123",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("Password1234");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException(
                        "Пользователь с таким телефоном уже существует."
                ));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(passwordEncoder).encode("Password123");
        verify(userService).registerUser(any(User.class));
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void register_shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordEncoder, userService, jwtService);
    }

    @Test
    void login_shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(
                "ivan@example.com",
                "Password123"
        );

        User user = new User(
                "ivan@example.com",
                "Password1234",
                "Ivan",
                "Ivanov",
                "+79991234567"
        );

        user.assignUserRole();

        when(userRepository.findByEmail("ivan@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(any(), any()))
                .thenReturn("jwt-token");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(userRepository).findByEmail("ivan@example.com");

        verify(jwtService).generateToken(
                "ivan@example.com",
                Role.USER.name()
        );
    }

    @Test
    void login_shouldReturnUnauthorized_whenAuthenticationFails() throws Exception {
        LoginRequest request = new LoginRequest(
                "ivan@example.com",
                "WrongPassword123"
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Неверный email или пароль"));

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    void login_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        LoginRequest request = new LoginRequest(
                "ivan@example.com",
                "Password123"
        );

        when(userRepository.findByEmail("ivan@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(userRepository).findByEmail("ivan@example.com");
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest(
                "",
                ""
        );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, userRepository, jwtService);
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest(
                " ",
                "Password123"
        );

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, userRepository, jwtService);
    }

    @Test
    void login_shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, userRepository, jwtService);
    }

    @Test
    void test_shouldReturnTestString() throws Exception {
        mockMvc.perform(get("/api/v1/auth/test"))
                .andExpect(status().isOk());
    }
}