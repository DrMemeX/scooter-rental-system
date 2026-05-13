package ru.senla.scooterrental.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;
import ru.senla.scooterrental.fleet.exceptions.InvalidRentalPointStateException;

@Entity
@Table(name = "rental_points")
public class RentalPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_node_id", nullable = false)
    private LocationNode locationNode;

    @Column(nullable = false)
    private boolean active;

    protected RentalPoint() {
    }

    public RentalPoint(String name, LocationNode locationNode) {
        this.name = requireNotBlank(name, "Название точки проката");
        this.locationNode = validateLocationNode(locationNode);
        this.active = true;
    }

    public boolean canAcceptScooter() {
        return active
                && locationNode.isActive()
                && locationNode.isRentalPoint();
    }

    public boolean canReleaseScooter() {
        return active
                && locationNode.isActive()
                && locationNode.isRentalPoint();
    }

    public void activate() {
        if (!locationNode.isActive()) {
            throw new InvalidRentalPointStateException(
                    "Нельзя активировать точку проката внутри неактивной родительской локации"
            );
        }

        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void rename(String name) {
        this.name = requireNotBlank(name, "Название точки проката");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocationNode getLocationNode() {
        return locationNode;
    }

    public boolean isActive() {
        return active;
    }

    private LocationNode validateLocationNode(LocationNode locationNode) {
        if (locationNode == null) {
            throw new FleetValidationException(
                    "Узел локации точки проката не может быть пустым"
            );
        }

        if (!locationNode.isRentalPoint()) {
            throw new FleetValidationException(
                    "Точка проката должна быть привязана к узлу типа RENTAL_POINT"
            );
        }

        if (!locationNode.isActive()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может быть создана в неактивной локации"
            );
        }

        return locationNode;
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