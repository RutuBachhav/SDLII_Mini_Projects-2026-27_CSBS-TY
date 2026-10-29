package com.campuscare.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class ComplaintUpdateRequest implements Serializable {

    @NotBlank(message = "Message is required")
    private String message;

    private String newStatus; // Optional: PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED

    public ComplaintUpdateRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
