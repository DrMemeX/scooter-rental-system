package ru.senla.scooterrental.rental.entity;

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
import ru.senla.scooterrental.discount.entity.PromoCode;
import ru.senla.scooterrental.fleet.entity.Scooter;
import ru.senla.scooterrental.rental.enums.RentalStatus;
import ru.senla.scooterrental.rental.enums.TariffType;
import ru.senla.scooterrental.rental.enums.TerminationReason;
import ru.senla.scooterrental.rental.exceptions.InvalidRentalStateException;
import ru.senla.scooterrental.rental.exceptions.RentalValidationException;
import ru.senla.scooterrental.user.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RentalStatus status;

    @Column(name = "start_time", nullable = false, updatable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "tariff_type", nullable = false, length = 50)
    private TariffType tariffType;

    @Column(name = "planned_hours")
    private Integer plannedHours;

    @Column(name = "max_allowed_minutes")
    private Integer maxAllowedMinutes;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason", length = 50)
    private TerminationReason terminationReason;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    protected Rental() { }

    public Rental(User user,
                  Scooter scooter,
                  TariffType tariffType,
                  Integer plannedHours) {

        this.user = requireNonNull(user, "Пользователь");
        this.scooter = requireNonNull(scooter, "Самокат");
        this.tariffType = requireNonNull(tariffType, "Тип тарифа");

        if (tariffType == TariffType.HOUR) {
            this.plannedHours = requirePositiveInteger(
                    plannedHours,
                    "Количество часов"
            );
        } else {
            this.plannedHours = null;
        }

        this.status = RentalStatus.ACTIVE;
        this.startTime = LocalDateTime.now();
        this.endTime = null;
        this.maxAllowedMinutes = null;
        this.totalCost = BigDecimal.ZERO;
        this.promoCode = null;
        this.discountAmount = BigDecimal.ZERO;
        this.terminationReason = null;
        this.distanceKm = 0;
    }

    public void setMaxAllowedMinutes(Integer maxAllowedMinutes) {
        if (tariffType != TariffType.MINUTE && tariffType != TariffType.HOUR) {
            throw new RentalValidationException(
                    "Максимальное время поездки может быть задано только для тарифов с оплатой по времени"
            );
        }

        this.maxAllowedMinutes = requirePositiveInteger(
                maxAllowedMinutes,
                "Максимальное количество минут поездки"
        );
    }

    public void recordDistance(double distanceKm) {
        this.distanceKm = requireNonNegativeDistance(
                distanceKm,
                "Дистанция поездки"
        );
    }

    public void applyPromoCode(PromoCode promoCode, BigDecimal discountAmount) {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Промокод можно применить только к активной аренде"
            );
        }

        if (this.promoCode != null) {
            throw new RentalValidationException(
                    "Промокод уже применён к аренде"
            );
        }

        this.promoCode = requireNonNull(promoCode, "Промокод");
        this.discountAmount = requireNonNegative(discountAmount, "Размер скидки");
    }

    public void finish(BigDecimal totalCost, TerminationReason terminationReason) {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidRentalStateException(
                    "Завершить можно только активную аренду"
            );
        }

        this.totalCost = requireNonNegative(totalCost, "Итоговая стоимость аренды");
        this.terminationReason = requireNonNull(
                terminationReason,
                "Причина завершения аренды"
        );
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

    public void approveManualFinish(BigDecimal totalCost,
                                    TerminationReason terminationReason) {
        if (status != RentalStatus.PENDING_MANAGER_CONFIRMATION) {
            throw new InvalidRentalStateException(
                    "Подтвердить ручное завершение можно только для аренды, ожидающей проверки менеджера"
            );
        }

        this.totalCost = requireNonNegative(totalCost, "Итоговая стоимость аренды");
        this.terminationReason = requireNonNull(
                terminationReason,
                "Причина завершения аренды"
        );
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

    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public Scooter getScooter() {
        return scooter;
    }

    public Long getScooterId() {
        return scooter.getId();
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

    public PromoCode getPromoCode() {
        return promoCode;
    }

    public Long getPromoCodeId() {
        if (promoCode == null) {
            return null;
        }

        return promoCode.getId();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public TerminationReason getTerminationReason() {
        return terminationReason;
    }

    public double getDistanceKm() {
        return distanceKm;
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

    private double requireNonNegativeDistance(double value, String name) {
        if (value < 0) {
            throw new RentalValidationException(
                    name + " не может быть отрицательной"
            );
        }

        return value;
    }
}