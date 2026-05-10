package ru.senla.scooterrental.fleet.entity;

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
import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;

@Entity
@Table(name = "location_nodes")
public class LocationNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private LocationNode parent;

    @Column(nullable = false)
    private boolean active;

    protected LocationNode() { }

    public LocationNode(String name,
                        LocationType type,
                        LocationNode parent) {
        this.name = requireNotBlank(name, "Название локации");
        this.type = requireNonNull(type, "Тип локации");
        this.parent = parent;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocationType getType() {
        return type;
    }

    public LocationNode getParent() {
        return parent;
    }

    public boolean isActive() {
        return active;
    }

    public void rename(String name) {
        this.name = requireNotBlank(name, "Название локации");
    }

    public void changeParent(LocationNode parent) {
        this.parent = parent;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isCity() {
        return type == LocationType.CITY;
    }

    public boolean isDistrict() {
        return type == LocationType.DISTRICT;
    }

    public boolean isRentalPoint() {
        return type == LocationType.RENTAL_POINT;
    }

    public boolean belongsTo(LocationNode possibleParent) {
        if (possibleParent == null || parent == null) {
            return false;
        }

        return parent.getId() != null
                && parent.getId().equals(possibleParent.getId());
    }

    private <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new FleetValidationException(
                    name + " не может быть пустым"
            );
        }

        return value;
    }

    private String requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new FleetValidationException(
                    name + " не может быть пустым"
            );
        }

        return value.trim();
    }
}