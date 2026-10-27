package com.pms.dao;

import com.pms.model.ApplicationView;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * applications + placement_results access. List queries JOIN companies,
 * students and results so callers never loop with extra queries (no N+1).
 */
public final class ApplicationDao {

    public static final List<String> STATUSES =
            List.of("APPLIED", "SHORTLISTED", "INTERVIEW", "SELECTED", "REJECTED");

    private static final String BASE =
            "SELECT a.application_id, a.student_id, a.company_id, a.application_date, a.status,"
            + " c.company_name, c.job_role,"
            + " s.full_name, s.roll_number, s.branch, u.email,"
            + " r.interview_date, r.final_status, r.remarks"
            + " FROM applications a"
            + " JOIN companies c ON c.company_id = a.company_id"
            + " JOIN students s ON s.student_id = a.student_id"
            + " JOIN users u ON u.user_id = s.user_id"
            + " LEFT JOIN placement_results r ON r.application_id = a.application_id";

    private ApplicationDao() {}

    public static ApplicationView map(ResultSet rs) throws SQLException {
        return new ApplicationView(rs.getLong("application_id"), rs.getLong("student_id"),
                rs.getLong("company_id"), rs.getString("company_name"),
                rs.getString("job_role"), rs.getDate("application_date"),
                rs.getString("status"), rs.getString("full_name"),
                rs.getString("roll_number"), rs.getString("branch"),
                rs.getString("email"), rs.getDate("interview_date"),
                rs.getString("final_status"), rs.getString("remarks"));
    }

    public static List<ApplicationView> listByStudentId(Connection con, long studentId)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                BASE + " WHERE a.student_id = ? ORDER BY a.application_id DESC")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ApplicationView> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    /** Admin view with optional status/company filters. Max 500 rows. */
    public static List<ApplicationView> listAll(Connection con, String status, Long companyId)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE + " WHERE 1=1");
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status = ?");
        }
        if (companyId != null) {
            sql.append(" AND a.company_id = ?");
        }
        sql.append(" ORDER BY a.application_id DESC LIMIT 500");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (status != null && !status.isBlank()) {
                ps.setString(i++, status);
            }
            if (companyId != null) {
                ps.setLong(i++, companyId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ApplicationView> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    public static boolean exists(Connection con, long studentId, long companyId)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM applications WHERE student_id = ? AND company_id = ?")) {
            ps.setLong(1, studentId);
            ps.setLong(2, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static long insert(Connection con, long studentId, long companyId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO applications (student_id, company_id, application_date, status)"
                + " VALUES (?, ?, CURDATE(), 'APPLIED')",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, studentId);
            ps.setLong(2, companyId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
        }
    }

    public static boolean ownsApplication(Connection con, long applicationId, long studentId)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM applications WHERE application_id = ? AND student_id = ?")) {
            ps.setLong(1, applicationId);
            ps.setLong(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int updateStatus(Connection con, long applicationId, String status)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE applications SET status = ? WHERE application_id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, applicationId);
            return ps.executeUpdate();
        }
    }

    /**
     * Keeps placement_results consistent with the application status inside the
     * caller's transaction. Terminal statuses (SELECTED/REJECTED) create or
     * refresh the result row; other statuses refresh an existing row if present.
     */
    public static void syncResult(Connection con, long applicationId, String status,
            Date interviewDate, String remarks) throws SQLException {
        Long resultId = null;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT result_id FROM placement_results WHERE application_id = ?")) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultId = rs.getLong(1);
                }
            }
        }
        boolean terminal = "SELECTED".equals(status) || "REJECTED".equals(status);
        if (resultId == null && !terminal) {
            return; // nothing to keep consistent yet
        }
        if (resultId == null) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO placement_results (application_id, interview_date,"
                    + " final_status, remarks) VALUES (?, ?, ?, ?)")) {
                ps.setLong(1, applicationId);
                if (interviewDate != null) {
                    ps.setDate(2, interviewDate);
                } else {
                    ps.setNull(2, Types.DATE);
                }
                ps.setString(3, status);
                ps.setString(4, remarks);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE placement_results SET interview_date = ?, final_status = ?,"
                    + " remarks = ? WHERE result_id = ?")) {
                if (interviewDate != null) {
                    ps.setDate(1, interviewDate);
                } else {
                    ps.setNull(1, Types.DATE);
                }
                ps.setString(2, status);
                ps.setString(3, remarks);
                ps.setLong(4, resultId);
                ps.executeUpdate();
            }
        }
    }
}
