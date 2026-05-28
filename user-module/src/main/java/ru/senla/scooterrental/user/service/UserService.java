package ru.senla.scooterrental.user.service;

import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {

    User registerUser(User user);

    User registerAdmin(User user);

    User getById(Long id);

    User getByEmail(String email);

    User getByPhone(String phone);

    List<User> getAllUsers();

    List<User> getUsersByRole(Role role);

    List<User> getUsersByStatus(UserStatus status);

    User updateProfile(Long id,
                       String firstName,
                       String lastName,
                       String phone);

    User changeEmail(Long id, String newEmail);

    User changePassword(Long id, String newPassword);

    User blockUser(Long id);

    User activateUser(Long id);

    User verifyUser(Long id);

    User addBalance(Long id, BigDecimal amount);

    User buyThreeDaySubscription(Long id);

    User subtractBalance(Long id, BigDecimal amount);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}