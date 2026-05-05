package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.fleet.service.FleetService;
import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.ActiveRentalAlreadyExistsException;
import ru.senla.scooterrental.rental.exceptions.RentalNotFoundException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.rental.repository.RentalRepository;

import java.math.BigDecimal;
import java.util.List;

public class RentalService {

    private final RentalRepository rentalRepository;
    private final FleetService fleetService;
    private final PricingService pricingService;

    public RentalService(RentalRepository rentalRepository,
                         FleetService fleetService,
                         PricingService pricingService) {
        if (rentalRepository == null) {
            throw new RentalValidationException(
                    "Репозиторий аренд не может быть пустым"
            );
        }

        if (fleetService == null) {
            throw new RentalValidationException(
                    "Сервис парка самокатов не может быть пустым"
            );
        }

        if (pricingService == null) {
            throw new RentalValidationException(
                    "Сервис расчёта стоимости не может быть пустым"
            );
        }

        this.rentalRepository = rentalRepository;
        this.fleetService = fleetService;
        this.pricingService = pricingService;
    }

    public Rental startRental(Long userId,
                              Long scooterId,
                              TariffType tariffType) {

        if (userId == null || userId <= 0) {
            throw new RentalValidationException(
                    "Некорректный ID пользователя"
            );
        }

        if (scooterId == null || scooterId <= 0) {
            throw new RentalValidationException(
                    "Некорректный ID самоката"
            );
        }

        if (tariffType == null) {
            throw new RentalValidationException(
                    "Тип тарифа не может быть пустым"
            );
        }

        rentalRepository.findActiveByUserId(userId)
                .ifPresent(rental -> {
                    throw new ActiveRentalAlreadyExistsException(
                            "У пользователя с ID " + userId + " уже есть активная аренда"
                    );
                });

        fleetService.rentScooter(scooterId);

        Rental rental = new Rental(userId, scooterId, tariffType);

        return rentalRepository.save(rental);
    }

    public Rental finishRental(Long rentalId,
                               Long rentalPointId) {
        Rental rental = getRentalOrThrow(rentalId);

        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        BigDecimal totalCost = pricingService.calculate(rental, scooter);

        rental.finish(totalCost);

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        return rentalRepository.save(rental);
    }

    public Rental requestManualFinish(Long rentalId) {
        Rental rental = getRentalOrThrow(rentalId);

        fleetService.requestReturnVerification(rental.getScooterId());

        rental.requestManualFinish();

        return rentalRepository.save(rental);
    }

    public Rental approveManualFinish(Long rentalId,
                                      Long rentalPointId) {
        Rental rental = getRentalOrThrow(rentalId);

        Scooter scooter = fleetService.getScooterById(rental.getScooterId());

        BigDecimal totalCost = pricingService.calculate(rental, scooter);

        rental.approveManualFinish(totalCost);

        fleetService.returnScooter(rental.getScooterId(), rentalPointId);

        return rentalRepository.save(rental);
    }

    public Rental getRentalOrThrow(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(
                        "Аренда с ID " + rentalId + " не найдена"
                        )
                );
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> getRentalsByUserId(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public List<Rental> getRentalsByScooterId(Long scooterId) {
        return rentalRepository.findByScooterId(scooterId);
    }
}