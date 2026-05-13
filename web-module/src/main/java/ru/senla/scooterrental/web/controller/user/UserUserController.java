package ru.senla.scooterrental.web.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable("userId") Long userId,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);
        return UserWebMapper.toResponse(userService.getById(userId));
    }

    @PatchMapping("/{userId}/profile")
    public UserResponse updateProfile(
            @PathVariable("userId") Long userId,
            @RequestBody UpdateUserProfileRequest request,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);

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
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ChangeEmailRequest request,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);

        return UserWebMapper.toResponse(
                userService.changeEmail(userId, request.newEmail())
        );
    }

    @PatchMapping("/{userId}/password")
    public UserResponse changePassword(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);

        return UserWebMapper.toResponse(
                userService.changePassword(userId, request.newPassword())
        );
    }

    @PostMapping("/{userId}/balance")
    public UserResponse addBalance(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody BalanceRequest request,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);

        return UserWebMapper.toResponse(
                userService.addBalance(userId, request.amount())
        );
    }

    @PostMapping("/{userId}/subscription/three-days")
    public UserResponse buyThreeDaySubscription(
            @PathVariable("userId") Long userId,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);

        return UserWebMapper.toResponse(
                userService.buyThreeDaySubscription(userId)
        );
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable("userId") Long userId,
            Authentication authentication
    ) {
        ensureSelfOrAdmin(userId, authentication);
        userService.deleteUser(userId);
    }

    private void ensureSelfOrAdmin(Long targetUserId,
                                   Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Недостаточно прав");
        }
    }
}