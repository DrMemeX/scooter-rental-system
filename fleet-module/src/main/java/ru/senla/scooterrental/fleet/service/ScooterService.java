package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;

import java.util.List;

public interface ScooterService {

    Scooter createScooter(Long modelId, Long rentalPointId, double initialCharge);

    Scooter getScooterById(Long scooterId);

    Scooter rentScooter(Long scooterId);

    Scooter returnScooter(Long scooterId, Long rentalPointId);

    Scooter requestReturnVerification(Long scooterId);

    Scooter moveScooterToRentalPoint(Long scooterId, Long rentalPointId);

    Scooter sendToMaintenance(Long scooterId);

    Scooter completeMaintenance(Long scooterId);

    Scooter markServiceRequired(Long scooterId);

    Scooter chargeScooter(Long scooterId, double amount);

    Scooter addMileage(Long scooterId, double km);

    Scooter consumeCharge(Long scooterId, double amount);

    List<Scooter> findAllScooters();

    List<Scooter> findAvailableScooters();

    List<Scooter> findScootersByStatus(ScooterStatus status);

    List<Scooter> findScootersByRentalPoint(Long rentalPointId);

    void deleteScooter(Long scooterId);
}