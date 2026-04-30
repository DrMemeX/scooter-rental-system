package ru.senla.scooterrental.fleet.entity;

import ru.senla.scooterrental.fleet.enums.LocationType;

public class LocationNode {

    private final Long id;
    private final String name;
    private final LocationType type;
    private final LocationNode parent;

    private boolean active;

    public LocationNode(Long id,
                        String name,
                        LocationType type,
                        LocationNode parent) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор локации должен быть положительным"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Название локации не может быть пустым"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Тип локации не может быть пустым"
            );
        }

        if (type == LocationType.CITY && parent != null) {
            throw new IllegalArgumentException(
                    "Город не может иметь родительскую локацию"
            );
        }

        if (type == LocationType.DISTRICT &&
                (parent == null || !parent.isCity())) {
            throw new IllegalArgumentException(
                    "Район должен быть привязан к городу"
            );
        }

        if (type == LocationType.RENTAL_POINT &&
                (parent == null || !parent.isDistrict())) {
            throw new IllegalArgumentException(
                    "Точка проката должна быть привязана к району"
            );
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.active = true;
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

        if (possibleParent == null) {
            throw new IllegalArgumentException(
                    "Родительская локация для проверки не может быть пустой"
            );
        }

        LocationNode current = parent;

        while (current != null) {
            if (current.getId().equals(possibleParent.getId())) {
                return true;
            }

            current = current.getParent();
        }

        return false;
    }

    public void activate() {
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

    public LocationType getType() {
        return type;
    }

    public LocationNode getParent() {
        return parent;
    }

    public boolean isActive() {
        return active;
    }
}