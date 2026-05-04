package ru.senla.scooterrental.common.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    void deleteById(ID id);
}
