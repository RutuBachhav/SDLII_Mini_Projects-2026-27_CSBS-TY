package com.pms.dao;

import com.pms.model.Company;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** companies table access. */
public final class CompanyDao {

    private CompanyDao() {}

    public static Company map(ResultSet rs) throws SQLException {
        return new Company(rs.getLong("company_id"), rs.getString("company_name"),
                rs.getString("job_role"), rs.getBigDecimal("package_lpa"),
                rs.getBigDecimal("minimum_cgpa"), rs.getString("eligible_branches"),
                rs.getDate("application_deadline"), rs.getString("description"));
    }

    public static List<Company> listAll(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT company_id, company_name, job_role, package_lpa, minimum_cgpa,"
                + " eligible_branches, application_deadline, description FROM companies"
                + " ORDER BY application_deadline IS NULL, application_deadline ASC, company_id DESC");
                ResultSet rs = ps.executeQuery()) {
            List<Company> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public static Company findById(Connection con, long companyId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT company_id, company_name, job_role, package_lpa, minimum_cgpa,"
                + " eligible_branches, application_deadline, description FROM companies"
                + " WHERE company_id = ?")) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public static long insert(Connection con, String name, String jobRole, BigDecimal pkg,
            BigDecimal minCgpa, String branches, Date deadline, String description)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO companies (company_name, job_role, package_lpa, minimum_cgpa,"
                + " eligible_branches, application_deadline, description)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, jobRole);
            if (pkg != null) {
                ps.setBigDecimal(3, pkg);
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (minCgpa != null) {
                ps.setBigDecimal(4, minCgpa);
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, branches);
            if (deadline != null) {
                ps.setDate(6, deadline);
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, description);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        }
    }

    public static int update(Connection con, long companyId, String name, String jobRole,
            BigDecimal pkg, BigDecimal minCgpa, String branches, Date deadline, String description)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE companies SET company_name = ?, job_role = ?, package_lpa = ?,"
                + " minimum_cgpa = ?, eligible_branches = ?, application_deadline = ?,"
                + " description = ? WHERE company_id = ?")) {
            ps.setString(1, name);
            ps.setString(2, jobRole);
            if (pkg != null) {
                ps.setBigDecimal(3, pkg);
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            if (minCgpa != null) {
                ps.setBigDecimal(4, minCgpa);
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, branches);
            if (deadline != null) {
                ps.setDate(6, deadline);
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setString(7, description);
            ps.setLong(8, companyId);
            return ps.executeUpdate();
        }
    }

    public static long countApplications(Connection con, long companyId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM applications WHERE company_id = ?")) {
            ps.setLong(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public static int delete(Connection con, long companyId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM companies WHERE company_id = ?")) {
            ps.setLong(1, companyId);
            return ps.executeUpdate();
        }
    }
}
