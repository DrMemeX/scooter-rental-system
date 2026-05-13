package ru.senla.scooterrental.user.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;
import ru.senla.scooterrental.user.exceptions.UserValidationException;
import ru.senla.scooterrental.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User save(User user) {
        requireNonNull(user, "Пользователь");

        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }

        validateId(user.getId());
        return entityManager.merge(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        validateId(id);

        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return entityManager
                .createQuery(
                        "select u from User u",
                        User.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id);

        Long count = entityManager
                .createQuery(
                        "select count(u) from User u where u.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        User user = entityManager.find(User.class, id);

        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return entityManager
                .createQuery(
                        "select u from User u where lower(u.email) = :email",
                        User.class
                )
                .setParameter("email", normalizedEmail)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);

        return entityManager
                .createQuery(
                        "select u from User u where u.phone = :phone",
                        User.class
                )
                .setParameter("phone", normalizedPhone)
                .getResultStream()
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllByRole(Role role) {
        requireNonNull(role, "Роль пользователя");

        return entityManager
                .createQuery(
                        "select u from User u where u.role = :role",
                        User.class
                )
                .setParameter("role", role)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllByStatus(UserStatus status) {
        requireNonNull(status, "Статус пользователя");

        return entityManager
                .createQuery(
                        "select u from User u where u.status = :status",
                        User.class
                )
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        Long count = entityManager
                .createQuery(
                        "select count(u) from User u where lower(u.email) = :email",
                        Long.class
                )
                .setParameter("email", normalizedEmail)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        String normalizedPhone = normalizePhone(phone);

        Long count = entityManager
                .createQuery(
                        "select count(u) from User u where u.phone = :phone",
                        Long.class
                )
                .setParameter("phone", normalizedPhone)
                .getSingleResult();

        return count > 0;
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

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new UserValidationException(
                    name + " не задан."
            );
        }

        return obj;
    }
}