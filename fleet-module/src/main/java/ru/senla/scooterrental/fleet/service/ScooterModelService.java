package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.ScooterModel;

import java.math.BigDecimal;
import java.util.List;

public interface ScooterModelService {

    ScooterModel createScooterModel(
            ScooterClass scooterClass,
            double maxSpeedKmPerHour,
            double consumptionPerKm,
            BigDecimal pricePerMinute,
            BigDecimal pricePerHour,
            int batteryCapacity
    );

    ScooterModel getScooterModelById(Long modelId);

    ScooterModel updateScooterModelPrices(
            Long modelId,
            BigDecimal pricePerMinute,
            BigDecimal pricePerHour
    );

    List<ScooterModel> findAllScooterModels();

    void deleteScooterModel(Long modelId);
}