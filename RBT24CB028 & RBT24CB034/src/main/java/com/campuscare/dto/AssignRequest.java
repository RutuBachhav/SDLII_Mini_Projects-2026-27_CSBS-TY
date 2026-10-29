package com.campuscare.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class AssignRequest implements Serializable {

    @NotNull(message = "Staff ID is required")
    private Long staffId;

    private Long departmentId; // optional reassignment

    private String priority; // optional priority adjustment

    public AssignRequest() {
    }

    public AssignRequest(Long staffId) {
        this.staffId = staffId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
