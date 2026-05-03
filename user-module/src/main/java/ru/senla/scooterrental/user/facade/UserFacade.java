package ru.senla.scooterrental.user.facade;

import ru.senla.scooterrental.user.dto.BalanceRequest;
import ru.senla.scooterrental.user.dto.ChangeEmailRequest;
import ru.senla.scooterrental.user.dto.ChangePasswordRequest;
import ru.senla.scooterrental.user.dto.UserCreateRequest;
import ru.senla.scooterrental.user.dto.UserResponse;
import ru.senla.scooterrental.user.dto.UserUpdateRequest;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.mapper.UserMapper;
import ru.senla.scooterrental.user.service.UserService;

import java.util.List;

public class UserFacade {

    private final UserService userService;

    public UserFacade(UserService userService) {
        if (userService == null) {
            throw new UserValidationException(
                    "Сервис пользователей не может быть пустым."
            );
        }
        this.userService = userService;
    }

    public UserResponse registerUser(UserCreateRequest request) {
        if (request == null) {
            throw new UserValidationException(
                    "Запрос не может быть пустым."
            );
        }

        User user = UserMapper.toEntity(request);
        return UserMapper.toResponse(userService.registerUser(user));
    }

    public UserResponse registerManager(UserCreateRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toResponse(userService.registerManager(user));
    }

    public UserResponse getById(Long id) {
        return UserMapper.toResponse(userService.getById(id));
    }

    public UserResponse getByEmail(String email) {
        return UserMapper.toResponse(userService.getByEmail(email));
    }

    public UserResponse getByPhone(String phone) {
        return UserMapper.toResponse(userService.getByPhone(phone));
    }

    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userService.getUsersByRole(role).stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public List<UserResponse> getUsersByStatus(UserStatus status) {
        return userService.getUsersByStatus(status).stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse updateProfile(Long id, UserUpdateRequest request) {
        if (request.getFirstName() == null
            && request.getLastName() == null
            && request.getPhone() == null) {
            throw new UserValidationException(
                    "Запрос на обновление профиля не может быть пустым."
            );
        }

        User updatedUser = userService.updateProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );

        return UserMapper.toResponse(updatedUser);
    }

    public UserResponse changeEmail(Long id, ChangeEmailRequest request) {
        if (request == null || request.getNewEmail() == null) {
            throw new UserValidationException(
                    "Email не может быть пустым."
            );
        }

        return UserMapper.toResponse(
                userService.changeEmail(id, request.getNewEmail())
        );
    }

    public UserResponse changePassword(Long id, ChangePasswordRequest request) {
        if (request == null || request.getNewPassword() == null) {
            throw new UserValidationException(
                    "Пароль не может быть пустым."
            );
        }

        return UserMapper.toResponse(
                userService.changePassword(id, request.getNewPassword())
        );
    }

    public UserResponse verifyUser(Long id) {
        return UserMapper.toResponse(userService.verifyUser(id));
    }

    public UserResponse blockUser(Long id) {
        return UserMapper.toResponse(userService.blockUser(id));
    }

    public UserResponse activateUser(Long id) {
        return UserMapper.toResponse(userService.activateUser(id));
    }

    public UserResponse addBalance(Long id, BalanceRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new UserValidationException(
                    "Сумма не может быть пустой."
            );
        }

        return UserMapper.toResponse(
                userService.addBalance(id, request.getAmount())
        );
    }

    public UserResponse subtractBalance(Long id, BalanceRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new UserValidationException(
                    "Сумма не может быть пустой."
            );
        }

        return UserMapper.toResponse(
                userService.subtractBalance(id, request.getAmount())
        );
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
    }

    public boolean existsByEmail(String email) {
        return userService.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userService.existsByPhone(phone);
    }
}