package ru.senla.scooterrental.user.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    List<User> findAllByRole(Role role);

    List<User> findAllByStatus(UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}