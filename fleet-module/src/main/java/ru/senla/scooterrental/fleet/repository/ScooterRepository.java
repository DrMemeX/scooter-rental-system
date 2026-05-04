package ru.senla.scooterrental.fleet.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;

import java.util.List;

public interface ScooterRepository extends CrudRepository<Scooter, Long> {

    List<Scooter> findAllByStatus(ScooterStatus status);

    List<Scooter> findAllAvailable();

    List<Scooter> findAllByRentalPointId(Long rentalPointId);
}