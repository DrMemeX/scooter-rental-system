package ru.senla.scooterrental.rental.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaRentalRepository implements RentalRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Rental save(Rental rental) {
        validateRental(rental);

        if (rental.getId() == null) {
            entityManager.persist(rental);
            return rental;
        }

        validateId(rental.getId(), "ID аренды");
        return entityManager.merge(rental);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rental> findById(Long id) {
        validateId(id, "ID аренды");

        return Optional.ofNullable(
                entityManager.find(Rental.class, id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findAll() {
        return entityManager
                .createQuery(
                        "select rental from Rental rental",
                        Rental.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id, "ID аренды");

        Long count = entityManager
                .createQuery(
                        "select count(rental) from Rental rental where rental.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID аренды");

        Rental rental = entityManager.find(Rental.class, id);

        if (rental == null) {
            throw new RentalNotFoundException(
                    "Аренда с ID " + id + " не найдена"
            );
        }

        entityManager.remove(rental);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findByUserId(Long userId) {
        validateId(userId, "ID пользователя");

        return entityManager
                .createQuery(
                        """
                        select rental
                        from Rental rental
                        where rental.user.id = :userId
                        """,
                        Rental.class
                )
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findByScooterId(Long scooterId) {
        validateId(scooterId, "ID самоката");

        return entityManager
                .createQuery(
                        """
                        select rental
                        from Rental rental
                        where rental.scooter.id = :scooterId
                        """,
                        Rental.class
                )
                .setParameter("scooterId", scooterId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rental> findUnfinishedByUserId(Long userId) {
        validateId(userId, "ID пользователя");

        return entityManager
                .createQuery(
                        """
                        select rental
                        from Rental rental
                        where rental.user.id = :userId
                        and rental.status in (:activeStatus, :pendingStatus)
                        """,
                        Rental.class
                )
                .setParameter("userId", userId)
                .setParameter("activeStatus", RentalStatus.ACTIVE)
                .setParameter("pendingStatus", RentalStatus.PENDING_MANAGER_CONFIRMATION)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    private void validateRental(Rental rental) {
        if (rental == null) {
            throw new RentalValidationException(
                    "Аренда не может быть пустой"
            );
        }
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new RentalValidationException(
                    fieldName + " должен быть положительным"
            );
        }
    }
}