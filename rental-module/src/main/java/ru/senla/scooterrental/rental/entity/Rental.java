package ru.senla.scooterrental.rental.entity;

import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.exceptions.InvalidRentalStateException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Rental {

    private Long id;
    private final Long userId;
    private final Long scooterId;

    private RentalStatus status;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    private final TariffType tariffType;

    private BigDecimal totalCost;

    public Rental(Long userId,
                  Long scooterId,
                  TariffType tariffType) {

        if (userId == null || userId <= 0) {
            throw new RentalValidationException(
                    "ID пользователя должен быть положительным"
            );
        }

        if (scooterId == null || scooterId <= 0) {
            throw new RentalValidationException(
                    "ID самоката должен быть положительным"
            );
        }

        if (tariffType == null) {
            throw new RentalValidationException(
                    "Тип тарифа не может быть пустым"
            );
        }

        this.userId = userId;
        this.scooterId = scooterId;
        this.tariffType = tariffType;

        this.status = RentalStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        this.totalCost = BigDecimal.ZERO;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new RentalValidationException(
                    "ID аренды уже назначен"
            );
        }

        if (id == null || id <= 0) {
            throw new RentalValidationException(
                    "ID аренды должен быть положительным"
            );
        }

        this.id = id;
    }

    public void finish(BigDecimal totalCost) {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Завершить можно только активную аренду"
            );
        }

        if (totalCost == null) {
            throw new RentalValidationException(
                    "Итоговая стоимость аренды не может быть пустой"
            );
        }

        if (totalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new RentalValidationException(
                    "Итоговая стоимость аренды не может быть отрицательной"
            );
        }

        this.status = RentalStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        this.totalCost = totalCost;
    }

    public void requestManualFinish() {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Запросить ручное завершение можно только для активной аренды"
            );
        }

        this.status = RentalStatus.PENDING_MANAGER_CONFIRMATION;
    }

    public void approveManualFinish(BigDecimal totalCost) {
        if (status != RentalStatus.PENDING_MANAGER_CONFIRMATION) {
            throw new InvalidRentalStateException(
                    "Подтвердить ручное завершение можно только для аренды, ожидающей проверки менеджера"
            );
        }

        if (totalCost == null) {
            throw new RentalValidationException(
                    "Итоговая стоимость аренды не может быть пустой"
            );
        }

        if (totalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new RentalValidationException(
                    "Итоговая стоимость аренды не может быть отрицательной"
            );
        }

        this.status = RentalStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        this.totalCost = totalCost;
    }

    public void cancel() {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Отменить можно только активную аренду"
            );
        }

        this.status = RentalStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == RentalStatus.ACTIVE;
    }

    public boolean isFinished() {
        return status == RentalStatus.FINISHED;
    }

    public boolean isPendingManagerConfirmation() {
        return status == RentalStatus.PENDING_MANAGER_CONFIRMATION;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getScooterId() {
        return scooterId;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public TariffType getTariffType() {
        return tariffType;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}