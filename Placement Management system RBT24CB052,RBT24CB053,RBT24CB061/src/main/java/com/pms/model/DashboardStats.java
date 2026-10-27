package com.pms.model;

/**
 * Live counters for the admin dashboard and homepage stats.
 *
 * <p>Plain JavaBean (not a record): GlassFish's EL implementation resolves
 * JSP properties only through {@code getXxx()} getters.
 */
public final class DashboardStats {

    private final long students;
    private final long companies;
    private final long applications;
    private final long selected;

    public DashboardStats(long students, long companies, long applications, long selected) {
        this.students = students;
        this.companies = companies;
        this.applications = applications;
        this.selected = selected;
    }

    public long getStudents() {
        return students;
    }

    public long getCompanies() {
        return companies;
    }

    public long getApplications() {
        return applications;
    }

    public long getSelected() {
        return selected;
    }
}
