package ru.senla.scooterrental.user.dto;

public class ChangeEmailRequest {

    private String newEmail;

    public ChangeEmailRequest() {
    }

    public ChangeEmailRequest(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}