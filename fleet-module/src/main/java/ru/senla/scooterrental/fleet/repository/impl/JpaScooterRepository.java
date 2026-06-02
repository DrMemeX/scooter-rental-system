package ru.senla.scooterrental.fleet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaScooterRepository implements ScooterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Scooter save(Scooter scooter) {
        validateScooter(scooter);

        if (scooter.getId() == null) {
            entityManager.persist(scooter);
            return scooter;
        }

        validateId(scooter.getId());
        return entityManager.merge(scooter);
    }

    @Override
    public Optional<Scooter> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(entityManager.find(Scooter.class, id));
    }

    @Override
    public List<Scooter> findAll() {
        return entityManager
                .createQuery(
                        "select scooter from Scooter scooter",
                        Scooter.class
                )
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        validateId(id);

        Long count = entityManager
                .createQuery(
                        "select count(scooter) from Scooter scooter where scooter.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        Scooter scooter = entityManager.find(Scooter.class, id);

        if (scooter != null) {
            entityManager.remove(scooter);
        }
    }

    @Override
    public List<Scooter> findAllByStatus(ScooterStatus status) {
        if (status == null) {
            throw new FleetValidationException(
                    "Статус самоката не может быть пустым"
            );
        }

        return entityManager
                .createQuery(
                        "select scooter from Scooter scooter where scooter.status = :status",
                        Scooter.class
                )
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Scooter> findAllAvailable() {
        return entityManager
                .createQuery(
                        """
                        select scooter
                        from Scooter scooter
                        join fetch scooter.currentRentalPoint
                        where scooter.status = :status
                        and scooter.currentCharge >= 20
                        """,
                        Scooter.class
                )
                .setParameter("status", ScooterStatus.AVAILABLE)
                .getResultList();
    }

    @Override
    public List<Scooter> findAllByRentalPointId(Long rentalPointId) {
        validateId(rentalPointId);

        return entityManager
                .createQuery(
                        """
                        select scooter
                        from Scooter scooter
                        where scooter.currentRentalPoint.id = :rentalPointId
                        """,
                        Scooter.class
                )
                .setParameter("rentalPointId", rentalPointId)
                .getResultList();
    }

    @Override
    public List<Scooter> findAllByModelId(Long modelId) {
        return entityManager.createQuery("""
            SELECT s
            FROM Scooter s
            WHERE s.model.id = :modelId
            """, Scooter.class)
                .setParameter("modelId", modelId)
                .getResultList();
    }

    @Override
    public Optional<Scooter> findByIdForUpdate(Long id) {
        validateId(id);

        Scooter scooter = entityManager.find(
                Scooter.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );

        return Optional.ofNullable(scooter);
    }

    private void validateScooter(Scooter scooter) {
        if (scooter == null) {
            throw new FleetValidationException(
                    "Самокат не может быть пустым"
            );
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new FleetValidationException(
                    "ID должен быть положительным числом"
            );
        }
    }
}