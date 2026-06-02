package ru.senla.scooterrental.fleet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaScooterModelRepository implements ScooterModelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ScooterModel save(ScooterModel scooterModel) {
        validateScooterModel(scooterModel);

        if (scooterModel.getId() == null) {
            entityManager.persist(scooterModel);
            return scooterModel;
        }

        validateId(scooterModel.getId());
        return entityManager.merge(scooterModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScooterModel> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(entityManager.find(ScooterModel.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterModel> findAll() {
        return entityManager
                .createQuery(
                        "select model from ScooterModel model",
                        ScooterModel.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id);

        Long count = entityManager
                .createQuery(
                        "select count(model) from ScooterModel model where model.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        ScooterModel scooterModel = entityManager.find(ScooterModel.class, id);

        if (scooterModel != null) {
            entityManager.remove(scooterModel);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterModel> findAllByScooterClass(ScooterClass scooterClass) {
        if (scooterClass == null) {
            throw new FleetValidationException(
                    "Класс самоката не может быть пустым"
            );
        }

        return entityManager
                .createQuery(
                        "select model from ScooterModel model where model.scooterClass = :scooterClass",
                        ScooterModel.class
                )
                .setParameter("scooterClass", scooterClass)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByScooterClass(ScooterClass scooterClass) {
        Long count = entityManager
                .createQuery("""
                    select count(model)
                    from ScooterModel model
                    where model.scooterClass = :scooterClass
                    """, Long.class)
                .setParameter("scooterClass", scooterClass)
                .getSingleResult();

        return count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTechnicalAndPriceParameters(double consumptionPerKm,
                                                       BigDecimal pricePerMinute,
                                                       BigDecimal pricePerHour,
                                                       int batteryCapacity) {
        Long count = entityManager
                .createQuery("""
                    select count(model)
                    from ScooterModel model
                    where model.consumptionPerKm = :consumptionPerKm
                      and model.pricePerMinute = :pricePerMinute
                      and model.pricePerHour = :pricePerHour
                      and model.batteryCapacity = :batteryCapacity
                    """, Long.class)
                .setParameter("consumptionPerKm", consumptionPerKm)
                .setParameter("pricePerMinute", pricePerMinute)
                .setParameter("pricePerHour", pricePerHour)
                .setParameter("batteryCapacity", batteryCapacity)
                .getSingleResult();

        return count > 0;
    }

    private void validateScooterModel(ScooterModel scooterModel) {
        if (scooterModel == null) {
            throw new FleetValidationException(
                    "Модель самоката не может быть пустой"
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