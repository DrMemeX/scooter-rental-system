package ru.senla.scooterrental.maintenance.repository;

import ru.senla.scooterrental.common.repository.CrudRepository;
import ru.senla.scooterrental.maintenance.entity.ScooterServiceEvent;
import ru.senla.scooterrental.maintenance.enums.ServiceEventType;

import java.util.List;

public interface ServiceEventRepository
        extends CrudRepository<ScooterServiceEvent, Long> {

    List<ScooterServiceEvent> findAllByScooterId(Long scooterId);

    List<ScooterServiceEvent> findAllByType(ServiceEventType type);
}