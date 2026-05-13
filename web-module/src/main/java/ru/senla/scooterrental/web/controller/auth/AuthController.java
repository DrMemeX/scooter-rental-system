package ru.senla.scooterrental.web.controller.auth;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.exceptions.UserNotFoundException;
import ru.senla.scooterrental.user.repository.UserRepository;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.auth.LoginRequest;
import ru.senla.scooterrental.web.dto.request.auth.RegisterRequest;
import ru.senla.scooterrental.web.dto.response.auth.AuthResponse;
import ru.senla.scooterrental.web.security.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        log.info(
                "Registration request received: email={}",
                request.email()
        );

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                request.phone()
        );

        User savedUser = userService.registerUser(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        log.info(
                "Registration completed successfully: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail()
        );

        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        log.info(
                "Login request received: email={}",
                request.email()
        );

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с email " + request.email() + " не найден"
                ));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        log.info(
                "Login completed successfully: userId={}, email={}",
                user.getId(),
                user.getEmail()
        );

        return new AuthResponse(token);
    }

    @GetMapping("/test")
    public String test() {
        return "Тест";
    }
}