package com.campuscare.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class StatusChangeRequest implements Serializable {

    @NotBlank(message = "Status is required")
    private String status; // PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED

    private String remarks;

    public StatusChangeRequest() {
    }

    public StatusChangeRequest(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
