package com.pms.dao;

import com.pms.model.Student;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** students table access (always joined with users for email). */
public final class StudentDao {

    private static final String BASE =
            "SELECT s.student_id, s.user_id, s.roll_number, s.full_name, s.branch,"
            + " s.cgpa, s.graduation_year, s.skills, s.phone, u.email"
            + " FROM students s JOIN users u ON u.user_id = s.user_id";

    private StudentDao() {}

    public static Student map(ResultSet rs) throws SQLException {
        Integer year = (Integer) rs.getObject("graduation_year");
        return new Student(rs.getLong("student_id"), rs.getLong("user_id"),
                rs.getString("roll_number"), rs.getString("full_name"),
                rs.getString("branch"), rs.getBigDecimal("cgpa"), year,
                rs.getString("skills"), rs.getString("phone"), rs.getString("email"));
    }

    public static Student findByUserId(Connection con, long userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(BASE + " WHERE s.user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public static Student findByStudentId(Connection con, long studentId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(BASE + " WHERE s.student_id = ?")) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Only student-editable fields; branch/graduation year stay admin-controlled. */
    public static int updateProfile(Connection con, long studentId,
            String fullName, String phone, String skills) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET full_name = ?, phone = ?, skills = ? WHERE student_id = ?")) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, skills);
            ps.setLong(4, studentId);
            return ps.executeUpdate();
        }
    }

    /** Updates only the students.cgpa column. Callers own the transaction. */
    public static int updateCgpa(Connection con, long studentId, java.math.BigDecimal cgpa)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET cgpa = ? WHERE student_id = ?")) {
            ps.setBigDecimal(1, cgpa);
            ps.setLong(2, studentId);
            return ps.executeUpdate();
        }
    }

    /** Single-query admin list/search (name, roll, email, branch). Max 200 rows. */
    public static List<Student> search(Connection con, String query) throws SQLException {
        String sql = BASE;
        boolean hasFilter = query != null && !query.isBlank();
        if (hasFilter) {
            sql += " WHERE s.full_name LIKE ? OR s.roll_number LIKE ?"
                    + " OR u.email LIKE ? OR s.branch LIKE ?";
        }
        sql += " ORDER BY s.student_id DESC LIMIT 200";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasFilter) {
                String like = "%" + query.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }
}
