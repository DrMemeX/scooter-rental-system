package ru.senla.scooterrental.web.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.service.UserService;
import ru.senla.scooterrental.web.dto.request.user.BalanceRequest;
import ru.senla.scooterrental.web.dto.request.user.RegisterUserRequest;
import ru.senla.scooterrental.web.dto.response.user.UserResponse;
import ru.senla.scooterrental.web.mapper.UserWebMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerAdmin(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        User user = UserWebMapper.toEntity(request);
        User savedUser = userService.registerAdmin(user);

        return UserWebMapper.toResponse(savedUser);
    }

    @GetMapping
    public List<UserResponse> getUsers(
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "status", required = false) UserStatus status
    ) {
        List<User> users;

        if (role != null) {
            users = userService.getUsersByRole(role);
        } else if (status != null) {
            users = userService.getUsersByStatus(status);
        } else {
            users = userService.getAllUsers();
        }

        return users.stream()
                .map(UserWebMapper::toResponse)
                .toList();
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable("userId") Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.getById(userId)
        );
    }

    @PatchMapping("/{userId}/verify")
    public UserResponse verifyUser(
            @PathVariable("userId") Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.verifyUser(userId)
        );
    }

    @PatchMapping("/{userId}/block")
    public UserResponse blockUser(
            @PathVariable("userId") Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.blockUser(userId)
        );
    }

    @PatchMapping("/{userId}/activate")
    public UserResponse activateUser(
            @PathVariable("userId") Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.activateUser(userId)
        );
    }

    @PostMapping("/{userId}/balance")
    public UserResponse addBalance(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody BalanceRequest request
    ) {
        return UserWebMapper.toResponse(
                userService.addBalance(userId, request.amount())
        );
    }

    @PostMapping("/{userId}/subscription/three-days")
    public UserResponse buyThreeDaySubscription(
            @PathVariable("userId") Long userId
    ) {
        return UserWebMapper.toResponse(
                userService.buyThreeDaySubscription(userId)
        );
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable("userId") Long userId
    ) {
        userService.deleteUser(userId);
    }
}