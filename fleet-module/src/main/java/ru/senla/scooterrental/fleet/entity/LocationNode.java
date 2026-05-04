package ru.senla.scooterrental.fleet.entity;

import ru.senla.scooterrental.fleet.enums.LocationType;
import ru.senla.scooterrental.fleet.exceptions.FleetValidationException;

public class LocationNode {

    private final String name;
    private final LocationType type;
    private final LocationNode parent;

    private Long id;

    private boolean active;

    public LocationNode(String name,
                        LocationType type,
                        LocationNode parent) {

        if (name == null || name.isBlank()) {
            throw new FleetValidationException(
                    "Название локации не может быть пустым"
            );
        }

        if (type == null) {
            throw new FleetValidationException(
                    "Тип локации не может быть пустым"
            );
        }

        if (type == LocationType.CITY && parent != null) {
            throw new FleetValidationException(
                    "Город не может иметь родительскую локацию"
            );
        }

        if (type == LocationType.DISTRICT &&
                (parent == null || !parent.isCity())) {
            throw new FleetValidationException(
                    "Район должен быть привязан к городу"
            );
        }

        if (type == LocationType.RENTAL_POINT &&
                (parent == null || !parent.isDistrict())) {
            throw new FleetValidationException(
                    "Точка проката должна быть привязана к району"
            );
        }

        if (parent != null && !parent.isActive()) {
            throw new FleetValidationException(
                    "Нельзя создать локацию внутри неактивной родительской локации"
            );
        }

        this.name = name;
        this.type = type;
        this.parent = parent;
        this.active = true;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new FleetValidationException(
                    "ID локации уже назначен"
            );
        }

        if (id == null || id <= 0) {
            throw new FleetValidationException(
                    "ID локации должен быть положительным"
            );
        }

        this.id = id;
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
            throw new FleetValidationException(
                    "Родительская локация для проверки не может быть пустой"
            );
        }

        LocationNode current = this.parent;

        while (current != null) {
            if (current == possibleParent) {
                return true;
            }

            if (current.getId() != null
                    && possibleParent.getId() != null
                    && current.getId().equals(possibleParent.getId())) {
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