package ru.senla.scooterrental.user.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.UserAlreadyExistsException;
import ru.senla.scooterrental.user.exceptions.UserBlockedException;
import ru.senla.scooterrental.user.exceptions.UserNotFoundException;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.repository.UserRepository;
import ru.senla.scooterrental.user.service.UserService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private static final BigDecimal THREE_DAY_SUBSCRIPTION_PRICE =
            BigDecimal.valueOf(1499);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = requireNonNull(
                userRepository,
                "Репозиторий пользователей"
        );
    }

    @Override
    public User registerUser(User user) {
        validateUserObject(user);

        log.info("Registering user: email={}", user.getEmail());

        user.assignUserRole();
        User registeredUser = register(user);

        log.info(
                "User registered successfully: userId={}, email={}",
                registeredUser.getId(),
                registeredUser.getEmail()
        );

        return registeredUser;
    }

    @Override
    public User registerAdmin(User user) {
        validateUserObject(user);

        log.info("Registering manager: email={}", user.getEmail());

        user.assignAdminRole();
        User registeredManager = register(user);

        log.info(
                "Manager registered successfully: userId={}, email={}",
                registeredManager.getId(),
                registeredManager.getEmail()
        );

        return registeredManager;
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        validateId(id);

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с ID " + id + " не найден."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        validateText(email, "Email");

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с email " + email + " не найден."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByPhone(String phone) {
        validateText(phone, "Телефон");

        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException(
                        "Пользователь с телефоном " + phone + " не найден."
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        if (role == null) {
            throw new UserValidationException(
                    "Роль пользователя не может быть пустой."
            );
        }

        return userRepository.findAllByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByStatus(UserStatus status) {
        if (status == null) {
            throw new UserValidationException(
                    "Статус пользователя не может быть пустым."
            );
        }

        return userRepository.findAllByStatus(status);
    }

    @Override
    public User updateProfile(Long id,
                              String firstName,
                              String lastName,
                              String phone) {
        log.info("Updating user profile: userId={}", id);

        User user = getById(id);

        ensureUserIsActive(user);

        if (firstName == null && lastName == null && phone == null) {
            log.warn(
                    "User profile update rejected: userId={}, no data provided",
                    id
            );

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

        User savedUser = userRepository.save(user);

        log.info("User profile updated successfully: userId={}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User changeEmail(Long id, String newEmail) {
        log.info("Changing user email: userId={}", id);

        User user = getById(id);

        ensureUserIsActive(user);
        validateText(newEmail, "Email");

        String normalizedNewEmail = newEmail.trim().toLowerCase();

        if (!user.getEmail().equalsIgnoreCase(normalizedNewEmail)
                && userRepository.existsByEmail(normalizedNewEmail)) {
            log.warn(
                    "Email change rejected: userId={}, email={} already exists",
                    id,
                    normalizedNewEmail
            );

            throw new UserAlreadyExistsException(
                    "Пользователь с таким email уже существует."
            );
        }

        user.setEmail(normalizedNewEmail);
        User savedUser = userRepository.save(user);

        log.info(
                "User email changed successfully: userId={}, newEmail={}",
                savedUser.getId(),
                savedUser.getEmail()
        );

        return savedUser;
    }

    @Override
    public User changePassword(Long id, String newPassword) {
        log.info("Changing user password: userId={}", id);

        User user = getById(id);

        ensureUserIsActive(user);
        validateText(newPassword, "Пароль");

        user.changePassword(newPassword);
        User savedUser = userRepository.save(user);

        log.info("User password changed successfully: userId={}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User blockUser(Long id) {
        log.info("Blocking user: userId={}", id);

        User user = getById(id);

        if (user.isBlocked()) {
            log.warn("User block rejected: userId={} already blocked", id);

            throw new UserBlockedException(
                    "Пользователь уже заблокирован."
            );
        }

        user.block();
        User savedUser = userRepository.save(user);

        log.info("User blocked successfully: userId={}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User activateUser(Long id) {
        log.info("Activating user: userId={}", id);

        User user = getById(id);

        if (!user.isBlocked()) {
            log.warn("User activation rejected: userId={} already active", id);

            throw new UserValidationException(
                    "Пользователь уже активен."
            );
        }

        user.activate();
        User savedUser = userRepository.save(user);

        log.info("User activated successfully: userId={}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User verifyUser(Long id) {
        log.info("Verifying user: userId={}", id);

        User user = getById(id);

        ensureUserIsActive(user);

        if (user.isVerified()) {
            log.warn("User verification rejected: userId={} already verified", id);

            throw new UserValidationException(
                    "Пользователь уже верифицирован."
            );
        }

        user.verify();
        User savedUser = userRepository.save(user);

        log.info("User verified successfully: userId={}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User addBalance(Long id, BigDecimal amount) {
        log.info("Adding user balance: userId={}, amount={}", id, amount);

        User user = getById(id);

        ensureUserIsActive(user);

        user.addBalance(amount);
        User savedUser = userRepository.save(user);

        log.info(
                "User balance added successfully: userId={}, currentBalance={}",
                savedUser.getId(),
                savedUser.getBalance()
        );

        return savedUser;
    }

    @Override
    public User buyThreeDaySubscription(Long id) {
        log.info(
                "Buying three day subscription: userId={}, price={}",
                id,
                THREE_DAY_SUBSCRIPTION_PRICE
        );

        User user = getById(id);

        ensureUserIsActive(user);

        user.buyThreeDaySubscription(THREE_DAY_SUBSCRIPTION_PRICE);

        User savedUser = userRepository.save(user);

        log.info(
                "Three day subscription bought successfully: userId={}",
                savedUser.getId()
        );

        return savedUser;
    }

    @Override
    public User subtractBalance(Long id, BigDecimal amount) {
        log.info("Subtracting user balance: userId={}, amount={}", id, amount);

        User user = getById(id);

        ensureUserIsActive(user);

        user.subtractBalance(amount);
        User savedUser = userRepository.save(user);

        log.info(
                "User balance subtracted successfully: userId={}, currentBalance={}",
                savedUser.getId(),
                savedUser.getBalance()
        );

        return savedUser;
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user: userId={}", id);

        validateId(id);

        if (!userRepository.existsById(id)) {
            log.warn("User delete rejected: userId={} not found", id);

            throw new UserNotFoundException(
                    "Пользователь с ID " + id + " не найден."
            );
        }

        userRepository.deleteById(id);

        log.info("User deleted successfully: userId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        validateText(email, "Email");

        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
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
            log.warn("Operation rejected: userId={} is blocked", user.getId());

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
            log.warn(
                    "Phone update rejected: userId={}, phone={} already exists",
                    user.getId(),
                    normalizedNewPhone
            );

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
            log.warn(
                    "User registration rejected: email={} already exists",
                    user.getEmail()
            );

            throw new UserAlreadyExistsException(
                    "Пользователь с таким email уже существует."
            );
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            log.warn(
                    "User registration rejected: phone={} already exists",
                    user.getPhone()
            );

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