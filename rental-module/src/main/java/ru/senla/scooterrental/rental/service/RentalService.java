package ru.senla.scooterrental.rental.service;

import ru.senla.scooterrental.rental.entity.Rental;
import ru.senla.scooterrental.rental.enums.TariffType;

import java.util.List;

public interface RentalService {

    Rental startRental(Long userId,
                       Long scooterId,
                       TariffType tariffType,
                       Integer plannedHours);

    Rental startRental(Long userId,
                       Long scooterId,
                       TariffType tariffType);

    Rental finishRental(Long rentalId,
                        Long rentalPointId,
                        double distanceKm,
                        String promoCode);

    Rental finishRental(Long rentalId,
                        Long rentalPointId,
                        String promoCode);

    Rental finishRental(Long rentalId,
                        Long rentalPointId);

    Rental finishDueToBatteryDepleted(Long rentalId,
                                      Long rentalPointId,
                                      double distanceKm,
                                      String promoCode);

    Rental finishDueToBatteryDepleted(Long rentalId,
                                      Long rentalPointId,
                                      String promoCode);

    Rental finishDueToTechnicalBreakdown(Long rentalId,
                                         Long rentalPointId,
                                         double distanceKm,
                                         String promoCode);

    Rental finishDueToTechnicalBreakdown(Long rentalId,
                                         Long rentalPointId,
                                         String promoCode);

    Rental finishDueToPaymentLimitExceeded(Long rentalId,
                                           Long rentalPointId);

    Rental finishDueToUserDamage(Long rentalId,
                                 Long rentalPointId,
                                 double distanceKm,
                                 String promoCode);

    Rental finishDueToUserDamage(Long rentalId,
                                 Long rentalPointId,
                                 String promoCode);

    Rental requestManualFinish(Long rentalId);

    Rental approveManualFinish(Long rentalId,
                               Long rentalPointId,
                               double distanceKm,
                               String promoCode);

    Rental approveManualFinish(Long rentalId,
                               Long rentalPointId,
                               String promoCode);

    Rental approveManualFinish(Long rentalId,
                               Long rentalPointId);

    Rental getRentalOrThrow(Long rentalId);

    List<Rental> getAllRentals();

    List<Rental> getRentalsByUserId(Long userId);

    List<Rental> getRentalsByScooterId(Long scooterId);
}