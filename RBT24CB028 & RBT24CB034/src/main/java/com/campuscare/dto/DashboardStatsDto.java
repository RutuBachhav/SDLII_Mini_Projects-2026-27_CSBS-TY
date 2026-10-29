package com.campuscare.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DashboardStatsDto implements Serializable {

    private long totalComplaints;
    private long pendingComplaints;
    private long assignedComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private long totalStudents;
    private long totalStaff;
    private long totalDepartments;
    private Map<String, Long> complaintsByCategory = new HashMap<>();
    private Map<String, Long> complaintsByDepartment = new HashMap<>();
    private Map<String, Long> complaintsByPriority = new HashMap<>();

    public DashboardStatsDto() {
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getPendingComplaints() {
        return pendingComplaints;
    }

    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }

    public long getAssignedComplaints() {
        return assignedComplaints;
    }

    public void setAssignedComplaints(long assignedComplaints) {
        this.assignedComplaints = assignedComplaints;
    }

    public long getInProgressComplaints() {
        return inProgressComplaints;
    }

    public void setInProgressComplaints(long inProgressComplaints) {
        this.inProgressComplaints = inProgressComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getClosedComplaints() {
        return closedComplaints;
    }

    public void setClosedComplaints(long closedComplaints) {
        this.closedComplaints = closedComplaints;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalStaff() {
        return totalStaff;
    }

    public void setTotalStaff(long totalStaff) {
        this.totalStaff = totalStaff;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public Map<String, Long> getComplaintsByCategory() {
        return complaintsByCategory;
    }

    public void setComplaintsByCategory(Map<String, Long> complaintsByCategory) {
        this.complaintsByCategory = complaintsByCategory;
    }

    public Map<String, Long> getComplaintsByDepartment() {
        return complaintsByDepartment;
    }

    public void setComplaintsByDepartment(Map<String, Long> complaintsByDepartment) {
        this.complaintsByDepartment = complaintsByDepartment;
    }

    public Map<String, Long> getComplaintsByPriority() {
        return complaintsByPriority;
    }

    public void setComplaintsByPriority(Map<String, Long> complaintsByPriority) {
        this.complaintsByPriority = complaintsByPriority;
    }
}
