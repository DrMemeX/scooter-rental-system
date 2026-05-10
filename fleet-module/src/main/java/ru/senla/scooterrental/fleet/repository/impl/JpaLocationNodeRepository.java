package ru.senla.scooterrental.fleet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.LocationNodeRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class JpaLocationNodeRepository implements LocationNodeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public LocationNode save(LocationNode locationNode) {
        validateLocationNode(locationNode);

        if (locationNode.getId() == null) {
            entityManager.persist(locationNode);
            return locationNode;
        }

        validateId(locationNode.getId());
        return entityManager.merge(locationNode);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationNode> findById(Long id) {
        validateId(id);
        return Optional.ofNullable(entityManager.find(LocationNode.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findAll() {
        return entityManager
                .createQuery(
                        "select location from LocationNode location",
                        LocationNode.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        validateId(id);

        Long count = entityManager
                .createQuery(
                        "select count(location) from LocationNode location where location.id = :id",
                        Long.class
                )
                .setParameter("id", id)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public void deleteById(Long id) {
        validateId(id);

        LocationNode locationNode = entityManager.find(LocationNode.class, id);

        if (locationNode != null) {
            entityManager.remove(locationNode);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findAllByType(LocationType type) {
        if (type == null) {
            throw new FleetValidationException(
                    "Тип локации не может быть пустым"
            );
        }

        return entityManager
                .createQuery(
                        "select location from LocationNode location where location.type = :type",
                        LocationNode.class
                )
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findAllActive() {
        return entityManager
                .createQuery(
                        "select location from LocationNode location where location.active = true",
                        LocationNode.class
                )
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationNode> findAllByParentId(Long parentId) {
        validateId(parentId);

        return entityManager
                .createQuery(
                        "select location from LocationNode location where location.parent.id = :parentId",
                        LocationNode.class
                )
                .setParameter("parentId", parentId)
                .getResultList();
    }

    private void validateLocationNode(LocationNode locationNode) {
        if (locationNode == null) {
            throw new FleetValidationException(
                    "Локация не может быть пустой"
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