package ru.senla.scooterrental.maintenance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.repository.ServiceEventRepository;

import java.util.List;

@Service
@Transactional
public class MaintenanceService {

    private static final Logger log =
            LoggerFactory.getLogger(MaintenanceService.class);

    private final ServiceEventRepository serviceEventRepository;
    private final FleetService fleetService;

    public MaintenanceService(ServiceEventRepository serviceEventRepository,
                              FleetService fleetService) {
        this.serviceEventRepository = requireNonNull(
                serviceEventRepository,
                "Репозиторий сервисных событий"
        );
        this.fleetService = requireNonNull(
                fleetService,
                "Сервис парка самокатов"
        );
    }

    public ScooterServiceEvent reportTechnicalBreakdown(Long scooterId,
                                                        String description) {

        log.info(
                "Reporting technical breakdown: scooterId={}, description={}",
                scooterId,
                description
        );

        fleetService.markServiceRequired(scooterId);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.TECHNICAL_BREAKDOWN,
                description
        );

        log.info(
                "Technical breakdown reported successfully: scooterId={}, eventId={}",
                scooterId,
                event.getId()
        );

        return event;
    }

    public ScooterServiceEvent reportUserDamage(Long scooterId,
                                                String description) {

        log.info(
                "Reporting user damage: scooterId={}, description={}",
                scooterId,
                description
        );

        fleetService.markServiceRequired(scooterId);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.USER_DAMAGE,
                description
        );

        log.info(
                "User damage reported successfully: scooterId={}, eventId={}",
                scooterId,
                event.getId()
        );

        return event;
    }

    public ScooterServiceEvent sendToMaintenance(Long scooterId,
                                                 String description) {

        log.info(
                "Sending scooter to maintenance: scooterId={}, description={}",
                scooterId,
                description
        );

        fleetService.sendToMaintenance(scooterId);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.SENT_TO_MAINTENANCE,
                description
        );

        log.info(
                "Scooter sent to maintenance successfully: scooterId={}, eventId={}",
                scooterId,
                event.getId()
        );

        return event;
    }

    public ScooterServiceEvent completeMaintenance(Long scooterId,
                                                   String description) {

        log.info(
                "Completing scooter maintenance: scooterId={}, description={}",
                scooterId,
                description
        );

        fleetService.completeMaintenance(scooterId);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.MAINTENANCE_COMPLETED,
                description
        );

        log.info(
                "Scooter maintenance completed successfully: scooterId={}, eventId={}",
                scooterId,
                event.getId()
        );

        return event;
    }

    public ScooterServiceEvent chargeScooter(Long scooterId,
                                             double amount,
                                             String description) {

        log.info(
                "Charging scooter: scooterId={}, amount={}, description={}",
                scooterId,
                amount,
                description
        );

        if (amount <= 0) {
            log.warn(
                    "Scooter charge rejected: scooterId={}, invalidAmount={}",
                    scooterId,
                    amount
            );

            throw new MaintenanceValidationException(
                    "Объем зарядки должен быть положительным"
            );
        }

        fleetService.chargeScooter(scooterId, amount);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.CHARGED,
                description
        );

        log.info(
                "Scooter charged successfully: scooterId={}, eventId={}, amount={}",
                scooterId,
                event.getId(),
                amount
        );

        return event;
    }

    public ScooterServiceEvent markServiceRequired(Long scooterId,
                                                   String description) {

        log.info(
                "Marking scooter service required: scooterId={}, description={}",
                scooterId,
                description
        );

        fleetService.markServiceRequired(scooterId);

        ScooterServiceEvent event = createEvent(
                scooterId,
                ServiceEventType.SERVICE_REQUIRED,
                description
        );

        log.info(
                "Scooter marked service required successfully: scooterId={}, eventId={}",
                scooterId,
                event.getId()
        );

        return event;
    }

    public List<ScooterServiceEvent> getAllEvents() {
        return serviceEventRepository.findAll();
    }

    public List<ScooterServiceEvent> getEventsByScooterId(Long scooterId) {
        validatePositiveId(scooterId, "ID самоката");

        return serviceEventRepository.findAllByScooterId(scooterId);
    }

    public List<ScooterServiceEvent> getEventsByType(ServiceEventType type) {
        if (type == null) {
            log.warn("Service events request rejected: type is null");

            throw new MaintenanceValidationException(
                    "Тип сервисного события не может быть пустым"
            );
        }

        return serviceEventRepository.findAllByType(type);
    }

    private ScooterServiceEvent createEvent(Long scooterId,
                                            ServiceEventType type,
                                            String description) {
        Scooter scooter = fleetService.getScooterById(scooterId);

        ScooterServiceEvent event = new ScooterServiceEvent(
                scooter,
                type,
                description
        );

        return serviceEventRepository.save(event);
    }

    private void validatePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new MaintenanceValidationException(
                    name + " должен быть положительным"
            );
        }
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new MaintenanceValidationException(
                    name + " не задан"
            );
        }

        return obj;
    }
}