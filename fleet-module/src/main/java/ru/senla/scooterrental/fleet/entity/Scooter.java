package ru.senla.scooterrental.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.exceptions.InvalidRentalPointStateException;
import ru.senla.scooterrental.fleet.exceptions.InvalidScooterStateException;
import ru.senla.scooterrental.fleet.exceptions.ScooterUnavailableException;

@Entity
@Table(name = "scooters")
public class Scooter {

    private static final double MIN_REQUIRED_CHARGE_FOR_RENTAL = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private ScooterModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScooterStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_point_id")
    private RentalPoint currentRentalPoint;

    @Column(name = "current_charge", nullable = false)
    private double currentCharge;

    @Column(name = "total_mileage_km", nullable = false)
    private double totalMileageKm;

    protected Scooter() {
    }

    public Scooter(ScooterModel model,
                   RentalPoint currentRentalPoint,
                   double currentCharge) {
        this.model = validateModel(model);
        this.currentRentalPoint = validateRentalPoint(currentRentalPoint);
        validateCurrentCharge(currentCharge, model);

        this.currentCharge = currentCharge;
        this.status = hasEnoughChargeForRental()
                ? ScooterStatus.AVAILABLE
                : ScooterStatus.SERVICE_REQUIRED;
        this.totalMileageKm = 0;
    }

    public boolean isAvailable() {
        return status == ScooterStatus.AVAILABLE
                && hasEnoughChargeForRental();
    }

    public boolean hasEnoughChargeForRental() {
        return currentCharge >= MIN_REQUIRED_CHARGE_FOR_RENTAL;
    }

    public void markAsRented() {
        if (status != ScooterStatus.AVAILABLE) {
            throw new ScooterUnavailableException(
                    "Самокат недоступен для аренды"
            );
        }

        if (!hasEnoughChargeForRental()) {
            throw new ScooterUnavailableException(
                    "Для начала аренды требуется минимум 20 единиц заряда"
            );
        }

        if (currentRentalPoint == null || !currentRentalPoint.canReleaseScooter()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может выдать самокат"
            );
        }

        status = ScooterStatus.RENTED;
        currentRentalPoint = null;
    }

    public void returnToPoint(RentalPoint point) {
        if (point == null) {
            throw new FleetValidationException(
                    "Точка проката не может быть пустой"
            );
        }

        if (!point.canAcceptScooter()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может принять самокат"
            );
        }

        if (status != ScooterStatus.RENTED
                && status != ScooterStatus.RETURN_VERIFICATION_REQUIRED) {
            throw new InvalidScooterStateException(
                    "Возврат возможен только для арендованного самоката или самоката на ручной проверке"
            );
        }

        currentRentalPoint = point;

        status = hasEnoughChargeForRental()
                ? ScooterStatus.AVAILABLE
                : ScooterStatus.SERVICE_REQUIRED;
    }

    public void requireReturnVerification() {
        if (status != ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Только арендованный самокат можно перевести на ручной возврат"
            );
        }

        status = ScooterStatus.RETURN_VERIFICATION_REQUIRED;
    }

    public void sendToMaintenance() {
        if (status == ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Нельзя отправить в обслуживание самокат, который находится в аренде"
            );
        }

        if (status == ScooterStatus.MAINTENANCE) {
            throw new InvalidScooterStateException(
                    "Самокат уже находится на обслуживании"
            );
        }

        status = ScooterStatus.MAINTENANCE;
    }

    public void completeMaintenance() {
        if (status != ScooterStatus.MAINTENANCE) {
            throw new InvalidScooterStateException(
                    "Завершить обслуживание можно только для самоката в ремонте"
            );
        }

        status = hasEnoughChargeForRental()
                ? ScooterStatus.AVAILABLE
                : ScooterStatus.SERVICE_REQUIRED;
    }

    public void markServiceRequired() {
        if (status == ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Нельзя пометить арендованный самокат как требующий обслуживания"
            );
        }

        if (status == ScooterStatus.MAINTENANCE) {
            throw new InvalidScooterStateException(
                    "Самокат уже находится на обслуживании"
            );
        }

        status = ScooterStatus.SERVICE_REQUIRED;
    }

    public void charge(double amount) {
        if (amount <= 0) {
            throw new FleetValidationException(
                    "Объем зарядки должен быть положительным"
            );
        }

        if (status == ScooterStatus.RENTED
                || status == ScooterStatus.MAINTENANCE) {
            throw new InvalidScooterStateException(
                    "Нельзя заряжать арендованный или находящийся в ремонте самокат"
            );
        }

        currentCharge = Math.min(
                model.getBatteryCapacity(),
                currentCharge + amount
        );

        if (status == ScooterStatus.SERVICE_REQUIRED
                && hasEnoughChargeForRental()) {
            status = ScooterStatus.AVAILABLE;
        }
    }

    public void consumeCharge(double amount) {
        if (amount <= 0) {
            throw new FleetValidationException(
                    "Расход заряда должен быть положительным"
            );
        }

        if (status != ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Расход заряда возможен только во время аренды"
            );
        }

        currentCharge = Math.max(0, currentCharge - amount);

        if (currentCharge <= 0) {
            status = ScooterStatus.RETURN_VERIFICATION_REQUIRED;
        }
    }

    public void addMileage(double km) {
        if (km <= 0) {
            throw new FleetValidationException(
                    "Пробег должен быть положительным"
            );
        }

        if (status != ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Увеличение пробега возможно только во время аренды"
            );
        }

        totalMileageKm += km;
    }

    public Long getId() {
        return id;
    }

    public ScooterModel getModel() {
        return model;
    }

    public ScooterStatus getStatus() {
        return status;
    }

    public RentalPoint getCurrentRentalPoint() {
        return currentRentalPoint;
    }

    public double getCurrentCharge() {
        return currentCharge;
    }

    public double getTotalMileageKm() {
        return totalMileageKm;
    }

    private ScooterModel validateModel(ScooterModel model) {
        if (model == null) {
            throw new FleetValidationException(
                    "Модель самоката не может быть пустой"
            );
        }

        return model;
    }

    private RentalPoint validateRentalPoint(RentalPoint currentRentalPoint) {
        if (currentRentalPoint == null) {
            throw new FleetValidationException(
                    "Точка проката не может быть пустой"
            );
        }

        if (!currentRentalPoint.canAcceptScooter()) {
            throw new InvalidRentalPointStateException(
                    "Самокат нельзя разместить в данной точке проката"
            );
        }

        return currentRentalPoint;
    }

    private void validateCurrentCharge(double currentCharge, ScooterModel model) {
        if (currentCharge < 0) {
            throw new FleetValidationException(
                    "Заряд не может быть отрицательным"
            );
        }

        if (currentCharge > model.getBatteryCapacity()) {
            throw new FleetValidationException(
                    "Заряд не может превышать емкость батареи"
            );
        }
    }
}