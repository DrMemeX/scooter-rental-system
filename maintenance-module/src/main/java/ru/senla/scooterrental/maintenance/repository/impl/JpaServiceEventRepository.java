package ru.senla.scooterrental.maintenance.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.repository.ServiceEventRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaServiceEventRepository implements ServiceEventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ScooterServiceEvent save(ScooterServiceEvent event) {
        validateEvent(event);

        if (event.getId() == null) {
            entityManager.persist(event);
            return event;
        }

        validateId(event.getId(), "ID сервисного события");
        return entityManager.merge(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScooterServiceEvent> findById(Long id) {
        validateId(id, "ID сервисного события");

        return Optional.ofNullable(
                entityManager.find(ScooterServiceEvent.class, id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterServiceEvent> findAll() {
        return entityManager
                .createQuery(
                        """
                        select event
                        from ScooterServiceEvent event
                        join fetch event.scooter
                        """,
                        ScooterServiceEvent.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id, "ID сервисного события");

        Long count = entityManager
                .createQuery(
                        "select count(event) from ScooterServiceEvent event where event.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id, "ID сервисного события");

        ScooterServiceEvent event = entityManager.find(
                ScooterServiceEvent.class,
                id
        );

        if (event != null) {
            entityManager.remove(event);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterServiceEvent> findAllByScooterId(Long scooterId) {
        validateId(scooterId, "ID самоката");

        return entityManager
                .createQuery(
                        """
                        select event
                        from ScooterServiceEvent event
                        join fetch event.scooter
                        where event.scooter.id = :scooterId
                        """,
                        ScooterServiceEvent.class
                )
                .setParameter("scooterId", scooterId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterServiceEvent> findAllByType(ServiceEventType type) {
        if (type == null) {
            throw new MaintenanceValidationException(
                    "Тип сервисного события не может быть пустым"
            );
        }

        return entityManager
                .createQuery(
                        """
                        select event
                        from ScooterServiceEvent event
                        join fetch event.scooter
                        where event.type = :type
                        """,
                        ScooterServiceEvent.class
                )
                .setParameter("type", type)
                .getResultList();
    }

    private void validateEvent(ScooterServiceEvent event) {
        if (event == null) {
            throw new MaintenanceValidationException(
                    "Сервисное событие не может быть пустым"
            );
        }
    }

    private void validateId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new MaintenanceValidationException(
                    name + " должен быть положительным"
            );
        }
    }
}