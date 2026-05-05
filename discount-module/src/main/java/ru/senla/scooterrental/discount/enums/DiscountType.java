package ru.senla.scooterrental.discount.enums;

import java.math.BigDecimal;

public enum DiscountType {

    STUDENT(BigDecimal.valueOf(5)),
    LOYALTY(BigDecimal.valueOf(10)),
    PREMIUM(BigDecimal.valueOf(15));

    private final BigDecimal percent;

    DiscountType(BigDecimal percent) {
        this.percent = percent;
    }

    public BigDecimal getPercent() {
        return percent;
    }
}