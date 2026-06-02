package ru.senla.scooterrental.fleet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.entity.ScooterModel;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetEntityNotFoundException;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.repository.ScooterRepository;
import ru.senla.scooterrental.fleet.service.RentalPointService;
import ru.senla.scooterrental.fleet.service.ScooterModelService;
import ru.senla.scooterrental.fleet.service.ScooterService;

import java.util.List;

@Service
@Transactional
public class ScooterServiceImpl implements ScooterService {

    private static final Logger log = LoggerFactory.getLogger(ScooterServiceImpl.class);

    private final ScooterRepository scooterRepository;
    private final ScooterModelService scooterModelService;
    private final RentalPointService rentalPointService;

    public ScooterServiceImpl(ScooterRepository scooterRepository,
                              ScooterModelService scooterModelService,
                              RentalPointService rentalPointService) {
        this.scooterRepository = requireNonNull(scooterRepository, "Репозиторий самокатов");
        this.scooterModelService = requireNonNull(scooterModelService, "Сервис моделей самокатов");
        this.rentalPointService = requireNonNull(rentalPointService, "Сервис точек проката");
    }

    @Override
    public Scooter createScooter(Long modelId, Long rentalPointId, double initialCharge) {
        log.info("Creating scooter: modelId={}, rentalPointId={}, initialCharge={}",
                modelId,
                rentalPointId,
                initialCharge
        );

        ScooterModel model = scooterModelService.getScooterModelById(modelId);
        RentalPoint rentalPoint = rentalPointService.getRentalPointById(rentalPointId);

        Scooter scooter = new Scooter(model, rentalPoint, initialCharge);
        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter created successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    @Transactional(readOnly = true)
    public Scooter getScooterById(Long scooterId) {
        return scooterRepository.findById(scooterId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Самокат с ID " + scooterId + " не найден"
                ));
    }

    @Override
    public Scooter rentScooter(Long scooterId) {
        log.info("Renting scooter: scooterId={}", scooterId);

        Scooter scooter = scooterRepository.findByIdForUpdate(scooterId)
                .orElseThrow(() -> new FleetEntityNotFoundException(
                        "Самокат с ID " + scooterId + " не найден"
                ));

        if (scooter.getStatus() != ScooterStatus.AVAILABLE) {
            log.warn("Scooter rent rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Самокат с ID " + scooterId + " недоступен для аренды"
            );
        }

        scooter.markAsRented();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter rented successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter returnScooter(Long scooterId, Long rentalPointId) {
        log.info("Returning scooter: scooterId={}, rentalPointId={}", scooterId, rentalPointId);

        Scooter scooter = getScooterById(scooterId);
        RentalPoint rentalPoint = rentalPointService.getRentalPointById(rentalPointId);

        if (scooter.getStatus() != ScooterStatus.RENTED
                && scooter.getStatus() != ScooterStatus.RETURN_VERIFICATION_REQUIRED) {
            log.warn("Scooter return rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Вернуть можно только арендованный самокат"
            );
        }

        scooter.returnToPoint(rentalPoint);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter returned successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter requestReturnVerification(Long scooterId) {
        log.info("Requesting scooter return verification: scooterId={}", scooterId);

        Scooter scooter = getScooterById(scooterId);

        if (scooter.getStatus() != ScooterStatus.RENTED) {
            log.warn("Return verification rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Запросить ручное завершение можно только для арендованного самоката"
            );
        }

        scooter.requireReturnVerification();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter return verification requested successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter moveScooterToRentalPoint(Long scooterId, Long rentalPointId) {
        log.info("Moving scooter to rental point: scooterId={}, rentalPointId={}",
                scooterId,
                rentalPointId
        );

        Scooter scooter = getScooterById(scooterId);
        RentalPoint rentalPoint = rentalPointService.getRentalPointById(rentalPointId);

        if (scooter.getStatus() == ScooterStatus.RENTED
                || scooter.getStatus() == ScooterStatus.RETURN_VERIFICATION_REQUIRED) {
            log.warn("Scooter move rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Нельзя перемещать самокат с активной арендой"
            );
        }

        scooter.moveToRentalPoint(rentalPoint);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter moved successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter sendToMaintenance(Long scooterId) {
        log.info("Sending scooter to maintenance: scooterId={}", scooterId);

        Scooter scooter = getScooterById(scooterId);
        scooter.sendToMaintenance();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter sent to maintenance successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter completeMaintenance(Long scooterId) {
        log.info("Completing scooter maintenance: scooterId={}", scooterId);

        Scooter scooter = getScooterById(scooterId);
        scooter.completeMaintenance();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter maintenance completed successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter markServiceRequired(Long scooterId) {
        log.info("Marking scooter service required: scooterId={}", scooterId);

        Scooter scooter = getScooterById(scooterId);
        scooter.markServiceRequired();

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter marked service required successfully: scooterId={}", savedScooter.getId());

        return savedScooter;
    }

    @Override
    public Scooter chargeScooter(Long scooterId, double amount) {
        log.info("Charging scooter: scooterId={}, amount={}", scooterId, amount);

        Scooter scooter = getScooterById(scooterId);
        scooter.charge(amount);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter charged successfully: scooterId={}, currentCharge={}",
                savedScooter.getId(),
                savedScooter.getCurrentCharge()
        );

        return savedScooter;
    }

    @Override
    public Scooter addMileage(Long scooterId, double km) {
        log.info("Adding scooter mileage: scooterId={}, km={}", scooterId, km);

        Scooter scooter = getScooterById(scooterId);
        scooter.addMileage(km);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter mileage added successfully: scooterId={}, totalMileage={}",
                savedScooter.getId(),
                savedScooter.getTotalMileageKm()
        );

        return savedScooter;
    }

    @Override
    public Scooter consumeCharge(Long scooterId, double amount) {
        log.info("Consuming scooter charge: scooterId={}, amount={}", scooterId, amount);

        Scooter scooter = getScooterById(scooterId);
        scooter.consumeCharge(amount);

        Scooter savedScooter = scooterRepository.save(scooter);

        log.info("Scooter charge consumed successfully: scooterId={}, currentCharge={}",
                savedScooter.getId(),
                savedScooter.getCurrentCharge()
        );

        return savedScooter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scooter> findAllScooters() {
        return scooterRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scooter> findAvailableScooters() {
        return scooterRepository.findAllAvailable();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scooter> findScootersByStatus(ScooterStatus status) {
        return scooterRepository.findAllByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Scooter> findScootersByRentalPoint(Long rentalPointId) {
        return scooterRepository.findAllByRentalPointId(rentalPointId);
    }

    @Override
    public void deleteScooter(Long scooterId) {
        log.info("Deleting scooter: scooterId={}", scooterId);

        Scooter scooter = getScooterById(scooterId);

        if (scooter.getStatus() == ScooterStatus.RENTED
                || scooter.getStatus() == ScooterStatus.RETURN_VERIFICATION_REQUIRED) {
            log.warn("Scooter delete rejected: scooterId={}, status={}",
                    scooterId,
                    scooter.getStatus()
            );

            throw new FleetValidationException(
                    "Нельзя удалить самокат, участвующий в активной аренде"
            );
        }

        scooterRepository.deleteById(scooterId);

        log.info("Scooter deleted successfully: scooterId={}", scooterId);
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new FleetValidationException(name + " не задан");
        }

        return obj;
    }
}