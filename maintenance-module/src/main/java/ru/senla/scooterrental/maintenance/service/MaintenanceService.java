package ru.senla.scooterrental.maintenance.service;

import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;
import ru.senla.scooterrental.maintenance.exceptions.MaintenanceValidationException;
import ru.senla.scooterrental.maintenance.repository.ServiceEventRepository;

import java.util.List;

public class MaintenanceService {

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
        fleetService.markServiceRequired(scooterId);

        return createEvent(
                scooterId,
                ServiceEventType.TECHNICAL_BREAKDOWN,
                description
        );
    }

    public ScooterServiceEvent reportUserDamage(Long scooterId,
                                                String description) {
        fleetService.markServiceRequired(scooterId);

        return createEvent(
                scooterId,
                ServiceEventType.USER_DAMAGE,
                description
        );
    }

    public ScooterServiceEvent sendToMaintenance(Long scooterId,
                                                 String description) {
        fleetService.sendToMaintenance(scooterId);

        return createEvent(
                scooterId,
                ServiceEventType.SENT_TO_MAINTENANCE,
                description
        );
    }

    public ScooterServiceEvent completeMaintenance(Long scooterId,
                                                   String description) {

        fleetService.completeMaintenance(scooterId);

        return createEvent(
                scooterId,
                ServiceEventType.MAINTENANCE_COMPLETED,
                description
        );
    }

    public ScooterServiceEvent chargeScooter(Long scooterId,
                                             double amount,
                                             String description) {
        if (amount <= 0) {
            throw new MaintenanceValidationException(
                    "Объем зарядки должен быть положительным"
            );
        }

        fleetService.chargeScooter(scooterId, amount);

        return createEvent(
                scooterId,
                ServiceEventType.CHARGED,
                description
        );
    }

    public ScooterServiceEvent markServiceRequired(Long scooterId,
                                                   String description) {
        fleetService.markServiceRequired(scooterId);

        return createEvent(
                scooterId,
                ServiceEventType.SERVICE_REQUIRED,
                description
        );
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
            throw new MaintenanceValidationException(
                    "Тип сервисного события не может быть пустым"
            );
        }

        return serviceEventRepository.findAllByType(type);
    }

    private ScooterServiceEvent createEvent(Long scooterId,
                                            ServiceEventType type,
                                            String description) {
        ScooterServiceEvent event = new ScooterServiceEvent(
                scooterId,
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