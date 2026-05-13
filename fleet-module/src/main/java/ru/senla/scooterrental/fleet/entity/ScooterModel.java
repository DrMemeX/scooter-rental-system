package ru.senla.scooterrental.fleet.entity;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;

import java.math.BigDecimal;

public class ScooterModel {

    private Long id;

    private final ScooterClass scooterClass;

    private final double maxSpeedKmPerHour;

    private final double consumptionPerKm;

    private final BigDecimal pricePerMinute;

    private final BigDecimal pricePerHour;

    private final int batteryCapacity;

    public ScooterModel(ScooterClass scooterClass,
                        double maxSpeedKmPerHour,
                        double consumptionPerKm,
                        BigDecimal pricePerMinute,
                        BigDecimal pricePerHour,
                        int batteryCapacity) {

        this.scooterClass = requireNonNull(scooterClass, "Класс самоката");
        this.maxSpeedKmPerHour = requirePositiveDouble(maxSpeedKmPerHour, "Максимальная скорость");
        this.consumptionPerKm = requirePositiveDouble(consumptionPerKm, "Расход заряда");
        this.pricePerMinute = requirePositiveMoney(pricePerMinute, "Цена за минуту");
        this.pricePerHour = requirePositiveMoney(pricePerHour, "Цена за час");
        this.batteryCapacity = requirePositiveInt(batteryCapacity, "Емкость батареи");
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new FleetValidationException(
                    "ID модели уже назначен"
            );
        }

        if (id == null || id <= 0) {
            throw new FleetValidationException(
                    "ID модели должен быть положительным"
            );
        }

        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public ScooterClass getScooterClass() {
        return scooterClass;
    }

    public double getMaxSpeedKmPerHour() {
        return maxSpeedKmPerHour;
    }

    public double getConsumptionPerKm() {
        return consumptionPerKm;
    }

    public BigDecimal getPricePerMinute() {
        return pricePerMinute;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    private <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new FleetValidationException(
                    name + " не задан"
            );
        }

        return value;
    }

    private double requirePositiveDouble(double value, String name) {
        if (value <= 0) {
            throw new FleetValidationException(
                    name + " должен быть положительным"
            );
        }

        return value;
    }

    private BigDecimal requirePositiveMoney(BigDecimal value, String name) {
        requireNonNull(value, name);

        if (value.signum() <= 0) {
            throw new FleetValidationException(
                    name + " должна быть положительной"
            );
        }

        return value;
    }

    private int requirePositiveInt(int value, String name) {
        if (value <= 0) {
            throw new FleetValidationException(
                    name + " должна быть положительной"
            );
        }

        return value;
    }
}