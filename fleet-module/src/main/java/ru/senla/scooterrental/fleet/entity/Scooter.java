package ru.senla.scooterrental.fleet.entity;

import ru.senla.scooterrental.fleet.enums.ScooterStatus;
import ru.senla.scooterrental.fleet.exceptions.InvalidRentalPointStateException;
import ru.senla.scooterrental.fleet.exceptions.InvalidScooterStateException;
import ru.senla.scooterrental.fleet.exceptions.ScooterUnavailableException;
import ru.senla.scooterrental.fleet.valueobject.ScooterModel;

public class Scooter {

    private static final double MIN_REQUIRED_CHARGE_FOR_RENTAL = 20;

    private final Long id;
    private final ScooterModel model;

    private ScooterStatus status;

    private RentalPoint currentRentalPoint;

    private double currentCharge;

    private double totalMileageKm;

    public Scooter(Long id,
                   ScooterModel model,
                   RentalPoint currentRentalPoint,
                   double currentCharge) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор самоката должен быть положительным"
            );
        }

        if (model == null) {
            throw new IllegalArgumentException(
                    "Модель самоката не может быть пустой"
            );
        }

        if (currentRentalPoint == null) {
            throw new IllegalArgumentException(
                    "Точка проката не может быть пустой"
            );
        }

        if (currentCharge < 0) {
            throw new IllegalArgumentException(
                    "Заряд не может быть отрицательным"
            );
        }

        if (currentCharge > model.getBatteryCapacity()) {
            throw new IllegalArgumentException(
                    "Заряд не может превышать емкость батареи"
            );
        }

        if (!currentRentalPoint.canAcceptScooter()) {
            throw new InvalidRentalPointStateException(
                    "Самокат нельзя разместить в данной точке проката"
            );
        }

        this.id = id;
        this.model = model;
        this.currentRentalPoint = currentRentalPoint;
        this.currentCharge = currentCharge;

        this.status = ScooterStatus.AVAILABLE;
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

        if (!isAvailable()) {
            throw new ScooterUnavailableException(
                    "Самокат недоступен для аренды"
            );
        }

        if (!hasEnoughChargeForRental()) {
            throw new ScooterUnavailableException(
                    "Для начала аренды требуется минимум 20 единиц заряда"
            );
        }

        if (!currentRentalPoint.canReleaseScooter()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может выдать самокат"
            );
        }

        status = ScooterStatus.RENTED;
    }

    public void returnToPoint(RentalPoint point) {

        if (point == null) {
            throw new IllegalArgumentException(
                    "Точка проката не может быть пустой"
            );
        }

        if (!point.canAcceptScooter()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может принять самокат"
            );
        }

        if (status != ScooterStatus.RENTED &&
                status != ScooterStatus.RETURN_VERIFICATION_REQUIRED) {
            throw new InvalidScooterStateException(
                    "Возврат возможен только для арендованного самоката или самоката на ручной проверке"
            );
        }

        currentRentalPoint = point;
        status = ScooterStatus.AVAILABLE;
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

        if (status == ScooterStatus.RENTED ||
            status == ScooterStatus.MAINTENANCE) {

            throw new InvalidScooterStateException(
                    "Нельзя отправить самокат на обслуживание"
            );
        }

        status = ScooterStatus.MAINTENANCE;
    }

    public void charge(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Объем зарядки должен быть положительным"
            );
        }

        if (status == ScooterStatus.RENTED || status == ScooterStatus.MAINTENANCE) {
            throw new InvalidScooterStateException(
                    "Нельзя заряжать арендованный или находящийся в ремонте самокат"
            );
        }

        currentCharge = Math.min(
                model.getBatteryCapacity(),
                currentCharge + amount
        );
    }

    public void consumeCharge(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Расход заряда должен быть положительным"
            );
        }

        if (status != ScooterStatus.RENTED) {
            throw new InvalidScooterStateException(
                    "Расход заряда возможен только во время аренды"
            );
        }

        currentCharge = Math.max(
                0,
                currentCharge - amount
        );
    }

    public void addMileage(double km) {

        if (km <= 0) {
            throw new IllegalArgumentException(
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
}