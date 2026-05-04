package ru.senla.scooterrental.fleet.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.fleet.entity.RentalPoint;

import java.util.List;

public interface RentalPointRepository extends CrudRepository<RentalPoint, Long> {

    List<RentalPoint> findAllActive();

    List<RentalPoint> findAllByLocationNodeId(Long locationNodeId);
}