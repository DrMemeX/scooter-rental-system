package ru.senla.scooterrental.web.controller.auth;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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

        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
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

        return new AuthResponse(token);
    }

    @GetMapping("/test")
    public String test() {
        return "Тест";
    }
}