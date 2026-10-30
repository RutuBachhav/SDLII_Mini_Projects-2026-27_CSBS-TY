package hackmate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data access object for the {@code users} table.
 *
 * <p>Every statement is a prepared statement, so user supplied text is never
 * concatenated into SQL.</p>
 */
public class UserDAO {

    private static final String INSERT_USER =
            "INSERT INTO users (full_name, email, password, skills) VALUES (?, ?, ?, ?)";

    private static final String SELECT_BY_EMAIL =
            "SELECT user_id, full_name, email, password, skills FROM users WHERE email = ?";

    private static final String SELECT_BY_ID =
            "SELECT user_id, full_name, email, skills FROM users WHERE user_id = ?";

    private static final String UPDATE_SKILLS =
            "UPDATE users SET skills = ? WHERE user_id = ?";

    /**
     * @return true when an account already uses this email address
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Creates a new account.
     *
     * @param hashedPassword the already hashed password, never plain text
     * @return the generated user id, or -1 when it could not be read
     */
    public int createUser(String fullName, String email, String hashedPassword, String skills)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);
            ps.setString(4, skills == null ? "" : skills);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    /**
     * Loads one account by email address.
     *
     * @return the account, or null when the email is unknown
     */
    public User findByEmail(String email) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setSkills(rs.getString("skills"));
                    return user;
                }
            }
            return null;
        }
    }

    /**
     * Loads one account by id.
     *
     * @return the account, or null when the id is unknown
     */
    public User findById(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setSkills(rs.getString("skills"));
                    return user;
                }
            }
            return null;
        }
    }

    /**
     * Updates the comma separated skill list of a user.
     */
    public boolean updateSkills(int userId, String skills) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_SKILLS)) {

            ps.setString(1, skills == null ? "" : skills);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates the display name and the skill list of a user.
     */
    public boolean updateProfile(int userId, String fullName, String skills) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, skills = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, skills == null ? "" : skills);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Stores a new password hash for a user.
     */
    public boolean updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
