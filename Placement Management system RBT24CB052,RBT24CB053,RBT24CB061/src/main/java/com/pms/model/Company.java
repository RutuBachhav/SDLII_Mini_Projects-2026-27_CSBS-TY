package com.pms.model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Read-only view of a company / job drive row.
 *
 * <p>Plain JavaBean (not a record): GlassFish's EL implementation resolves
 * JSP properties only through {@code getXxx()} getters.
 */
public final class Company {

    private final long companyId;
    private final String companyName;
    private final String jobRole;
    private final BigDecimal packageLpa;
    private final BigDecimal minimumCgpa;
    private final String eligibleBranches;
    private final Date applicationDeadline;
    private final String description;

    public Company(long companyId, String companyName, String jobRole,
            BigDecimal packageLpa, BigDecimal minimumCgpa, String eligibleBranches,
            Date applicationDeadline, String description) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.minimumCgpa = minimumCgpa;
        this.eligibleBranches = eligibleBranches;
        this.applicationDeadline = applicationDeadline;
        this.description = description;
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

    public BigDecimal getPackageLpa() {
        return packageLpa;
    }

    public BigDecimal getMinimumCgpa() {
        return minimumCgpa;
    }

    public String getEligibleBranches() {
        return eligibleBranches;
    }

    public Date getApplicationDeadline() {
        return applicationDeadline;
    }

    public String getDescription() {
        return description;
    }
}
