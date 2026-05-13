package ru.senla.scooterrental.rental.entity;

import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
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
    private final Integer plannedHours;
    private Integer maxAllowedMinutes;

    private BigDecimal totalCost;

    private TerminationReason terminationReason;

    public Rental(Long userId,
                  Long scooterId,
                  TariffType tariffType,
                  Integer plannedHours) {

        this.userId = requirePositiveId(userId, "ID пользователя");
        this.scooterId = requirePositiveId(scooterId, "ID самоката");
        this.tariffType = requireNonNull(tariffType, "Тип тарифа");

        if (tariffType == TariffType.HOUR) {
            this.plannedHours = requirePositiveInteger(plannedHours, "Количество часов");
        } else {
            this.plannedHours = null;
        }

        this.status = RentalStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        this.totalCost = BigDecimal.ZERO;
        this.terminationReason = null;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new RentalValidationException("ID аренды уже назначен");
        }

        this.id = requirePositiveId(id, "ID аренды");
    }

    public void setMaxAllowedMinutes(Integer maxAllowedMinutes) {
        if (tariffType != TariffType.MINUTE) {
            throw new RentalValidationException(
                    "Максимальное время поездки задаётся только для поминутного тарифа"
            );
        }

        this.maxAllowedMinutes = requirePositiveInteger(
                maxAllowedMinutes,
                "Максимальное количество минут поездки"
        );
    }

    public void finish(BigDecimal totalCost, TerminationReason terminationReason) {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Завершить можно только активную аренду"
            );
        }

        this.totalCost = requireNonNegative(totalCost, "Итоговая стоимость аренды");
        this.terminationReason = requireNonNull(terminationReason, "Причина завершения аренды");
        this.status = RentalStatus.FINISHED;
        this.endTime = LocalDateTime.now();
    }

    public void requestManualFinish() {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Запросить ручное завершение можно только для активной аренды"
            );
        }

        this.status = RentalStatus.PENDING_MANAGER_CONFIRMATION;
    }

    public void approveManualFinish(BigDecimal totalCost, TerminationReason terminationReason) {
        if (status != RentalStatus.PENDING_MANAGER_CONFIRMATION) {
            throw new InvalidRentalStateException(
                    "Подтвердить ручное завершение можно только для аренды, ожидающей проверки менеджера"
            );
        }

        this.totalCost = requireNonNegative(totalCost, "Итоговая стоимость аренды");
        this.terminationReason = requireNonNull(terminationReason, "Причина завершения аренды");
        this.status = RentalStatus.FINISHED;
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

    public Integer getPlannedHours() {
        return plannedHours;
    }

    public Integer getMaxAllowedMinutes() {
        return maxAllowedMinutes;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public TerminationReason getTerminationReason() {
        return terminationReason;
    }
    private Long requirePositiveId(Long id, String name) {
        if (id == null || id <= 0) {
            throw new RentalValidationException(
                    name + " должен быть положительным"
            );
        }
        return id;
    }

    private <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new RentalValidationException(
                    name + " не задан"
            );
        }
        return obj;
    }

    private BigDecimal requireNonNegative(BigDecimal value, String name) {
        if (value == null) {
            throw new RentalValidationException(
                    name + " не задан"
            );
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new RentalValidationException(
                    name + " не может быть отрицательным"
            );
        }

        return value;
    }

    private Integer requirePositiveInteger(Integer value, String name) {
        if (value == null || value <= 0) {
            throw new RentalValidationException(
                    name + " должно быть положительным"
            );
        }

        return value;
    }
}