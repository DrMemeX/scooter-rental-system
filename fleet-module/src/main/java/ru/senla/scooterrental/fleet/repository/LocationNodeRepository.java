package ru.senla.scooterrental.fleet.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.fleet.entity.LocationNode;
import ru.senla.scooterrental.fleet.enums.LocationType;

import java.util.List;

public interface LocationNodeRepository extends CrudRepository<LocationNode, Long> {

    List<LocationNode> findAllByType(LocationType type);

    List<LocationNode> findAllActive();

    List<LocationNode> findAllByParentId(Long parentId);
}