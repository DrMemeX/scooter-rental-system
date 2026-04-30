package ru.senla.scooterrental.fleet.entity;

import ru.senla.scooterrental.fleet.exceptions.InvalidRentalPointStateException;

public class RentalPoint {

    private final Long id;
    private final String name;
    private final LocationNode locationNode;

    private boolean active;

    public RentalPoint(Long id, String name, LocationNode locationNode) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор точки проката должен быть положительным"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Название точки проката не может быть пустым"
            );
        }

        if (locationNode == null) {
            throw new IllegalArgumentException(
                    "Узел локации точки проката не может быть пустым"
            );
        }

        if (!locationNode.isRentalPoint()) {
            throw new IllegalArgumentException(
                    "Точка проката должна быть привязана к узлу типа RENTAL_POINT"
            );
        }

        if (!locationNode.isActive()) {
            throw new InvalidRentalPointStateException(
                    "Точка проката не может быть создана в неактивной локации"
            );
        }

        this.id = id;
        this.name = name;
        this.locationNode = locationNode;
        this.active = true;
    }

    public boolean canAcceptScooter() {
        return active && locationNode.isActive() && locationNode.isRentalPoint();
    }

    public boolean canReleaseScooter() {
        return active && locationNode.isActive() && locationNode.isRentalPoint();
    }

    public void activate() {
        if (!locationNode.isActive()) {
            throw new InvalidRentalPointStateException(
                    "Нельзя активировать точку проката в неактивной локации"
            );
        }

        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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
}