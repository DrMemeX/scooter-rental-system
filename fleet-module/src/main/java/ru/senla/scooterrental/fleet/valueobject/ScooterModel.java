package ru.senla.scooterrental.fleet.valueobject;

import ru.senla.scooterrental.common.enums.ScooterClass;

import java.math.BigDecimal;

public class ScooterModel {

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

        if (scooterClass == null) {
            throw new IllegalArgumentException(
                    "Класс самоката не может быть пустым"
            );
        }

        if (maxSpeedKmPerHour <= 0) {
            throw new IllegalArgumentException(
                    "Максимальная скорость должна быть положительной"
            );
        }

        if (consumptionPerKm < 0) {
            throw new IllegalArgumentException(
                    "Расход заряда не может быть отрицательным"
            );
        }

        if (pricePerMinute == null || pricePerMinute.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Цена за минуту должна быть положительной"
            );
        }

        if (pricePerHour == null || pricePerHour.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Цена за час должна быть положительной"
            );
        }

        if (batteryCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Емкость батареи должна быть положительной"
            );
        }

        this.scooterClass = scooterClass;
        this.maxSpeedKmPerHour = maxSpeedKmPerHour;
        this.consumptionPerKm = consumptionPerKm;
        this.pricePerMinute = pricePerMinute;
        this.pricePerHour = pricePerHour;
        this.batteryCapacity = batteryCapacity;
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
}