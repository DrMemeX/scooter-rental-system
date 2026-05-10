package ru.senla.scooterrental.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;

import java.math.BigDecimal;

@Entity
@Table(name = "scooter_models")
public class ScooterModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScooterClass scooterClass;

    @Column(nullable = false)
    private double maxSpeedKmPerHour;

    @Column(nullable = false)
    private double consumptionPerKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerMinute;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(nullable = false)
    private int batteryCapacity;

    protected ScooterModel() { }

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