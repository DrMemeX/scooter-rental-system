package ru.senla.scooterrental.web.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.user.BalanceRequest;
import ru.senla.scooterrental.web.dto.request.user.ChangeEmailRequest;
import ru.senla.scooterrental.web.dto.request.user.ChangePasswordRequest;
import ru.senla.scooterrental.web.dto.request.user.RegisterUserRequest;
import ru.senla.scooterrental.web.dto.request.user.UpdateUserProfileRequest;
import ru.senla.scooterrental.web.dto.response.user.UserResponse;
import ru.senla.scooterrental.web.mapper.UserWebMapper;

@RestController
@RequestMapping("/api/v1/users")
public class UserUserController {

    private final UserService userService;

    public UserUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        User user = UserWebMapper.toEntity(request);
        User savedUser = userService.registerUser(user);

        return UserWebMapper.toResponse(savedUser);
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.getById(userId)
        );
    }

    @PatchMapping("/{userId}/profile")
    public UserResponse updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileRequest request
    ) {
        User user = userService.updateProfile(
                userId,
                request.firstName(),
                request.lastName(),
                request.phone()
        );

        return UserWebMapper.toResponse(user);
    }

    @PatchMapping("/{userId}/email")
    public UserResponse changeEmail(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeEmailRequest request
    ) {
        User user = userService.changeEmail(
                userId,
                request.newEmail()
        );

        return UserWebMapper.toResponse(user);
    }

    @PatchMapping("/{userId}/password")
    public UserResponse changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = userService.changePassword(
                userId,
                request.newPassword()
        );

        return UserWebMapper.toResponse(user);
    }

    @PostMapping("/{userId}/balance")
    public UserResponse addBalance(
            @PathVariable Long userId,
            @Valid @RequestBody BalanceRequest request
    ) {
        User user = userService.addBalance(
                userId,
                request.amount()
        );

        return UserWebMapper.toResponse(user);
    }

    @PostMapping("/{userId}/subscription/three-days")
    public UserResponse buyThreeDaySubscription(
            @PathVariable Long userId
    ) {
        User user = userService.buyThreeDaySubscription(userId);

        return UserWebMapper.toResponse(user);
    }
}