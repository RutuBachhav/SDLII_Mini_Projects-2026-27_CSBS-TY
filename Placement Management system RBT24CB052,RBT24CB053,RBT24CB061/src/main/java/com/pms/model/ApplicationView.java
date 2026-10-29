package com.pms.model;

import java.sql.Date;

/**
 * Single-row view of an application with its company and optional placement
 * result. Loaded with JOINs (no N+1).
 *
 * <p>Plain JavaBean (not a record): GlassFish's EL implementation resolves
 * JSP properties only through {@code getXxx()} getters.
 */
public final class ApplicationView {

    private final long applicationId;
    private final long studentId;
    private final long companyId;
    private final String companyName;
    private final String jobRole;
    private final Date applicationDate;
    private final String status;
    private final String fullName;
    private final String rollNumber;
    private final String branch;
    private final String email;
    private final Date interviewDate;
    private final String finalStatus;
    private final String remarks;

    public ApplicationView(long applicationId, long studentId, long companyId,
            String companyName, String jobRole, Date applicationDate, String status,
            String fullName, String rollNumber, String branch, String email,
            Date interviewDate, String finalStatus, String remarks) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.applicationDate = applicationDate;
        this.status = status;
        this.fullName = fullName;
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.email = email;
        this.interviewDate = interviewDate;
        this.finalStatus = finalStatus;
        this.remarks = remarks;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public long getStudentId() {
        return studentId;
    }

    public long getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getBranch() {
        return branch;
    }

    public String getEmail() {
        return email;
    }

    public Date getInterviewDate() {
        return interviewDate;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public String getRemarks() {
        return remarks;
    }
}
