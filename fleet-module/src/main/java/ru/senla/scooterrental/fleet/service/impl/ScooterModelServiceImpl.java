package ru.senla.scooterrental.fleet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterModelRepository;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.ScooterModelService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ScooterModelServiceImpl implements ScooterModelService {

    private static final Logger log = LoggerFactory.getLogger(ScooterModelServiceImpl.class);

    private final ScooterModelRepository scooterModelRepository;
    private final ScooterRepository scooterRepository;

    public ScooterModelServiceImpl(ScooterModelRepository scooterModelRepository,
                                   ScooterRepository scooterRepository) {
        this.scooterModelRepository = requireNonNull(
                scooterModelRepository,
                "Репозиторий моделей самокатов"
        );
        this.scooterRepository = requireNonNull(
                scooterRepository,
                "Репозиторий самокатов"
        );
    }

    @Override
    public ScooterModel createScooterModel(ScooterClass scooterClass,
                                           double maxSpeedKmPerHour,
                                           double consumptionPerKm,
                                           BigDecimal pricePerMinute,
                                           BigDecimal pricePerHour,
                                           int batteryCapacity) {
        log.info("Creating scooter model: scooterClass={}", scooterClass);

        if (scooterModelRepository.existsByScooterClass(scooterClass)) {
            throw new FleetValidationException(
                    "Модель самоката с таким названием уже существует"
            );
        }

        if (scooterModelRepository.existsByTechnicalAndPriceParameters(
                consumptionPerKm,
                pricePerMinute,
                pricePerHour,
                batteryCapacity
        )) {
            throw new FleetValidationException(
                    "Модель самоката с такими ценовыми и техническими параметрами уже существует"
            );
        }

        ScooterModel model = new ScooterModel(
                scooterClass,
                maxSpeedKmPerHour,
                consumptionPerKm,
                pricePerMinute,
                pricePerHour,
                batteryCapacity
        );

        ScooterModel savedModel = scooterModelRepository.save(model);

        log.info("Scooter model created successfully: modelId={}", savedModel.getId());

        return savedModel;
    }

    @Override
    @Transactional(readOnly = true)
    public ScooterModel getScooterModelById(Long modelId) {
        return scooterModelRepository.findById(modelId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Модель самоката с ID " + modelId + " не найдена"
                ));
    }

    @Override
    public ScooterModel updateScooterModelPrices(Long modelId,
                                                 BigDecimal pricePerMinute,
                                                 BigDecimal pricePerHour) {
        log.info("Updating scooter model prices: modelId={}", modelId);

        ScooterModel model = getScooterModelById(modelId);
        model.updatePrices(pricePerMinute, pricePerHour);

        ScooterModel savedModel = scooterModelRepository.save(model);

        log.info("Scooter model prices updated successfully: modelId={}", savedModel.getId());

        return savedModel;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScooterModel> findAllScooterModels() {
        return scooterModelRepository.findAll();
    }

    @Override
    public void deleteScooterModel(Long modelId) {
        log.info("Deleting scooter model: modelId={}", modelId);

        ScooterModel model = getScooterModelById(modelId);
        List<Scooter> scooters = scooterRepository.findAllByModelId(model.getId());

        if (!scooters.isEmpty()) {
            log.warn("Scooter model delete rejected: modelId={}, scootersCount={}",
                    modelId,
                    scooters.size()
            );

            throw new FleetValidationException(
                    "Нельзя удалить модель самоката, пока существуют самокаты этой модели"
            );
        }

        scooterModelRepository.deleteById(modelId);

        log.info("Scooter model deleted successfully: modelId={}", modelId);
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new FleetValidationException(name + " не задан");
        }

        return obj;
    }
}