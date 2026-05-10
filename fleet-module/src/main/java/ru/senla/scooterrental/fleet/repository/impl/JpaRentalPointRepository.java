package ru.senla.scooterrental.fleet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.RentalPointRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaRentalPointRepository implements RentalPointRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RentalPoint save(RentalPoint rentalPoint) {
        validateRentalPoint(rentalPoint);

        if (rentalPoint.getId() == null) {
            entityManager.persist(rentalPoint);
            return rentalPoint;
        }

        validateId(rentalPoint.getId());
        return entityManager.merge(rentalPoint);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalPoint> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(entityManager.find(RentalPoint.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findAll() {
        return entityManager
                .createQuery(
                        "select point from RentalPoint point",
                        RentalPoint.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id);

        Long count = entityManager
                .createQuery(
                        "select count(point) from RentalPoint point where point.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        RentalPoint rentalPoint = entityManager.find(RentalPoint.class, id);

        if (rentalPoint != null) {
            entityManager.remove(rentalPoint);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findAllActive() {
        return entityManager
                .createQuery(
                        "select point from RentalPoint point where point.active = true",
                        RentalPoint.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalPoint> findAllByLocationNodeId(Long locationNodeId) {
        validateId(locationNodeId);

        return entityManager
                .createQuery(
                        "select point from RentalPoint point where point.locationNode.id = :locationNodeId",
                        RentalPoint.class
                )
                .setParameter("locationNodeId", locationNodeId)
                .getResultList();
    }

    private void validateRentalPoint(RentalPoint rentalPoint) {
        if (rentalPoint == null) {
            throw new FleetValidationException(
                    "Точка проката не может быть пустой"
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