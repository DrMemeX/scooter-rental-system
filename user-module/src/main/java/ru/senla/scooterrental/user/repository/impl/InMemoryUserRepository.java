package ru.senla.scooterrental.user.repository.impl;

import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final ConcurrentHashMap<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user == null) {
            throw new UserValidationException(
                    "Пользователь не может быть пустым."
            );
        }

        if (user.getId() != null) {
            validateId(user.getId());
        }

        if (user.getId() == null) {
            user.assignId(idGenerator.getAndIncrement());
        }

        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id);
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);
        storage.remove(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return storage.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst();
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);

        return storage.values().stream()
                .filter(user -> user.getPhone().equals(normalizedPhone))
                .findFirst();
    }

    @Override
    public List<User> findAllByRole(Role role) {
        if (role == null) {
            throw new UserValidationException(
                    "Роль пользователя не может быть пустой."
            );
        }

        return storage.values().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    @Override
    public List<User> findAllByStatus(UserStatus status) {
        if (status == null) {
            throw new UserValidationException(
                    "Статус пользователя не может быть пустым."
            );
        }

        return storage.values().stream()
                .filter(user -> user.getStatus() == status)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return findByPhone(phone).isPresent();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new UserValidationException(
                    "ID пользователя должен быть положительным числом."
            );
        }
    }

    private String normalizeText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new UserValidationException(
                    fieldName + " не может быть пустым."
            );
        }

        return value.trim();
    }

    private String normalizePhone(String phone) {
        return normalizeText(phone, "Телефон")
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");
    }

    private String normalizeEmail(String email) {
        return normalizeText(email, "Email").toLowerCase();
    }
}