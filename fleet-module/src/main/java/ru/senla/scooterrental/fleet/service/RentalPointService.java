package ru.senla.scooterrental.fleet.service;

import ru.senla.scooterrental.fleet.entity.RentalPoint;
import ru.senla.scooterrental.fleet.entity.Scooter;

import java.util.List;

public interface RentalPointService {

    RentalPoint createRentalPoint(String name, Long locationNodeId);

    RentalPoint activateRentalPoint(Long rentalPointId);

    RentalPoint deactivateRentalPoint(Long rentalPointId);

    RentalPoint renameRentalPoint(Long rentalPointId, String name);

    void deleteRentalPoint(Long rentalPointId);

    RentalPoint getRentalPointById(Long rentalPointId);

    RentalPoint getActiveRentalPointById(Long rentalPointId);

    List<RentalPoint> findAllRentalPoints();

    List<RentalPoint> findActiveRentalPoints();

    List<RentalPoint> findRentalPointsByLocationNode(Long locationNodeId);

    List<RentalPoint> findActiveRentalPointsByLocationNode(Long locationNodeId);

    List<Scooter> getRentalPointScooters(Long rentalPointId);
}