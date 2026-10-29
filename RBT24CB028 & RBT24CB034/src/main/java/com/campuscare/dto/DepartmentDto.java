package com.campuscare.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class DepartmentDto implements Serializable {

    private Long id;

    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String description;
    private long staffCount;
    private long complaintCount;

    public DepartmentDto() {
    }

    public DepartmentDto(Long id, String departmentName, String description) {
        this.id = id;
        this.departmentName = departmentName;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStaffCount() {
        return staffCount;
    }

    public void setStaffCount(long staffCount) {
        this.staffCount = staffCount;
    }

    public long getComplaintCount() {
        return complaintCount;
    }

    public void setComplaintCount(long complaintCount) {
        this.complaintCount = complaintCount;
    }
}
