package ru.senla.scooterrental.user.dto;

import java.math.BigDecimal;

public class BalanceRequest {

    private BigDecimal amount;

    public BalanceRequest() {
    }

    public BalanceRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}