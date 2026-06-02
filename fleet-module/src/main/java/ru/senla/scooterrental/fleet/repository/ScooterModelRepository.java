package ru.senla.scooterrental.fleet.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;

import java.math.BigDecimal;
import java.util.List;

public interface ScooterModelRepository extends CrudRepository<ScooterModel, Long> {

    List<ScooterModel> findAllByScooterClass(ScooterClass scooterClass);

    boolean existsByScooterClass(ScooterClass scooterClass);

    boolean existsByTechnicalAndPriceParameters(
            double consumptionPerKm,
            BigDecimal pricePerMinute,
            BigDecimal pricePerHour,
            int batteryCapacity
    );
}