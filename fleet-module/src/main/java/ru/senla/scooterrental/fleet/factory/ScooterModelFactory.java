package ru.senla.scooterrental.fleet.factory;

import ru.senla.scooterrental.common.enums.ScooterClass;
import ru.senla.scooterrental.fleet.valueobject.ScooterModel;

import java.math.BigDecimal;

public final class ScooterModelFactory {

    private ScooterModelFactory() {
    }

    public static ScooterModel create(
            ScooterClass scooterClass
    ) {

        return switch (scooterClass) {

            case SLOW -> new ScooterModel(
                    ScooterClass.SLOW,
                    10,
                    1,
                    new BigDecimal("4"),
                    new BigDecimal("200"),
                    1000
            );

            case BASIC -> new ScooterModel(
                    ScooterClass.BASIC,
                    20,
                    2,
                    new BigDecimal("8"),
                    new BigDecimal("400"),
                    1000
            );

            case SPEEDY -> new ScooterModel(
                    ScooterClass.SPEEDY,
                    30,
                    3,
                    new BigDecimal("12"),
                    new BigDecimal("600"),
                    1000
            );
        };
    }
}