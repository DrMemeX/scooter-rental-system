package ru.senla.scooterrental.user.service;

import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.UserAlreadyExistsException;
import ru.senla.scooterrental.user.exceptions.UserBlockedException;
import ru.senla.scooterrental.user.exceptions.UserNotFoundException;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = requireNonNull(userRepository, "Репозиторий пользователей");
    }

    public User registerUser(User user) {
        validateUserObject(user);
        user.assignUserRole();
        return register(user);
    }

    public User registerManager(User user) {
        validateUserObject(user);
        user.assignManagerRole();
        return register(user);
    }

    public User getById(Long id) {
        validateId(id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + id + " не найден."
                ));
    }

    public User getByEmail(String email) {
        validateText(email, "Email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с email " + email + " не найден."
                ));
    }

    public User getByPhone(String phone) {
        validateText(phone, "Телефон");

        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с телефоном " + phone + " не найден."
                ));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        if (role == null) {
            throw new UserValidationException(
                    "Роль пользователя не может быть пустой."
            );
        }

        return userRepository.findAllByRole(role);
    }

    public List<User> getUsersByStatus(UserStatus status) {
        if (status == null) {
            throw new UserValidationException(
                    "Статус пользователя не может быть пустым."
            );
        }

        return userRepository.findAllByStatus(status);
    }

    public User updateProfile(Long id,
                              String firstName,
                              String lastName,
                              String phone) {
        User user = getById(id);

        ensureUserIsActive(user);

        if (firstName == null && lastName == null && phone == null) {
            throw new UserValidationException(
                    "Не переданы данные для обновления профиля."
            );
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }

        if (lastName != null) {
            user.setLastName(lastName);
        }

        if (phone != null) {
            validatePhoneUniquenessForUpdate(user, phone);
            user.setPhone(phone);
        }

        return userRepository.save(user);
    }

    public User changeEmail(Long id, String newEmail) {
        User user = getById(id);

        ensureUserIsActive(user);
        validateText(newEmail, "Email");

        String normalizedNewEmail = newEmail.trim().toLowerCase();

        if (!user.getEmail().equalsIgnoreCase(normalizedNewEmail)
                && userRepository.existsByEmail(normalizedNewEmail)) {
            throw new UserAlreadyExistsException(
                    "Пользователь с таким email уже существует."
            );
        }

        user.setEmail(normalizedNewEmail);
        return userRepository.save(user);
    }

    public User changePassword(Long id, String newPassword) {
        User user = getById(id);

        ensureUserIsActive(user);
        validateText(newPassword, "Пароль");

        user.changePassword(newPassword);
        return userRepository.save(user);
    }

    public User blockUser(Long id) {
        User user = getById(id);

        if (user.isBlocked()) {
            throw new UserBlockedException(
                    "Пользователь уже заблокирован."
            );
        }

        user.block();
        return userRepository.save(user);
    }

    public User activateUser(Long id) {
        User user = getById(id);

        if (!user.isBlocked()) {
            throw new UserValidationException(
                    "Пользователь уже активен."
            );
        }

        user.activate();
        return userRepository.save(user);
    }

    public User verifyUser(Long id) {
        User user = getById(id);

        ensureUserIsActive(user);

        if (user.isVerified()) {
            throw new UserValidationException(
                    "Пользователь уже верифицирован."
            );
        }

        user.verify();
        return userRepository.save(user);
    }

    public User addBalance(Long id, BigDecimal amount) {
        User user = getById(id);

        ensureUserIsActive(user);

        user.addBalance(amount);
        return userRepository.save(user);
    }

    public User subtractBalance(Long id, BigDecimal amount) {
        User user = getById(id);

        ensureUserIsActive(user);

        user.subtractBalance(amount);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        validateId(id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(
                    "Пользователь с ID " + id + " не найден."
            );
        }

        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        validateText(email, "Email");
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        validateText(phone, "Телефон");
        return userRepository.existsByPhone(phone);
    }

    private void validateUserObject(User user) {
        if (user == null) {
            throw new UserValidationException(
                    "Пользователь не может быть пустым."
            );
        }
    }

    private void validateUser(User user) {
        validateUserObject(user);

        validateText(user.getEmail(), "Email");
        validateText(user.getPassword(), "Пароль");
        validateText(user.getFirstName(), "Имя");
        validateText(user.getLastName(), "Фамилия");
        validateText(user.getPhone(), "Телефон");

        if (user.getRole() == null) {
            throw new UserValidationException(
                    "Роль пользователя не может быть пустой."
            );
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new UserValidationException(
                    "ID пользователя должен быть положительным числом."
            );
        }
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new UserValidationException(
                    fieldName + " не может быть пустым."
            );
        }
    }

    private void ensureUserIsActive(User user) {
        if (user.isBlocked()) {
            throw new UserBlockedException(
                    "Операция недоступна: пользователь заблокирован."
            );
        }
    }

    private void validatePhoneUniquenessForUpdate(User user, String newPhone) {
        validateText(newPhone, "Телефон");

        String normalizedNewPhone = normalizePhone(newPhone);

        if (!user.getPhone().equals(normalizedNewPhone)
                && userRepository.existsByPhone(normalizedNewPhone)) {
            throw new UserAlreadyExistsException(
                    "Пользователь с таким телефоном уже существует."
            );
        }
    }

    private String normalizePhone(String phone) {
        return phone.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");
    }

    private User register(User user) {
        validateUser(user);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с таким email уже существует."
            );
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с таким телефоном уже существует."
            );
        }

        return userRepository.save(user);
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new UserValidationException(
                    name + " не задан."
            );
        }
        return obj;
    }
}
