package hackmate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for the {@code team_requests} table.
 *
 * <p>Every statement is a prepared statement so user supplied text is never
 * concatenated into SQL.</p>
 */
public class RequestDAO {

    private static final String INSERT_REQUEST =
            "INSERT INTO team_requests (team_id, user_id, message, status) VALUES (?, ?, ?, 'Pending')";

    private static final String SELECT_PENDING =
            "SELECT tr.request_id, tr.team_id, tr.user_id, tr.message, tr.status, tr.requested_at, "
          + "       t.team_name, t.project_title, u.full_name, u.email, u.skills "
          + "FROM team_requests tr "
          + "JOIN teams t ON t.team_id = tr.team_id "
          + "JOIN users u ON u.user_id = tr.user_id "
          + "WHERE tr.team_id = ? "
          + "ORDER BY tr.requested_at DESC";

    private static final String SELECT_FOR_USER =
            "SELECT tr.request_id, tr.team_id, tr.user_id, tr.message, tr.status, tr.requested_at, "
          + "       t.team_name, t.project_title, u.full_name, u.email, u.skills "
          + "FROM team_requests tr "
          + "JOIN teams t ON t.team_id = tr.team_id "
          + "JOIN users u ON u.user_id = tr.user_id "
          + "WHERE tr.user_id = ? "
          + "ORDER BY tr.requested_at DESC";

    private static final String SELECT_FOR_LEADER =
            "SELECT tr.request_id, tr.team_id, tr.user_id, tr.message, tr.status, tr.requested_at, "
          + "       t.team_name, t.project_title, u.full_name, u.email, u.skills "
          + "FROM team_requests tr "
          + "JOIN teams t ON t.team_id = tr.team_id "
          + "JOIN users u ON u.user_id = tr.user_id "
          + "WHERE t.team_leader_id = ? "
          + "ORDER BY tr.status = 'Pending' DESC, tr.requested_at DESC";

    private static final String SELECT_ONE =
            "SELECT tr.request_id, tr.team_id, tr.user_id, tr.message, tr.status, tr.requested_at, "
          + "       t.team_name, t.project_title, u.full_name, u.email, u.skills "
          + "FROM team_requests tr "
          + "JOIN teams t ON t.team_id = tr.team_id "
          + "JOIN users u ON u.user_id = tr.user_id "
          + "WHERE tr.request_id = ?";

    private static final String UPDATE_STATUS =
            "UPDATE team_requests SET status = ? WHERE request_id = ?";

    private static final String INSERT_MEMBER =
            "INSERT INTO team_members (team_id, user_id, role) VALUES (?, ?, ?)";

    private static final String SELECT_EXISTING_REQUEST =
            "SELECT status FROM team_requests WHERE team_id = ? AND user_id = ? "
          + "ORDER BY request_id DESC LIMIT 1";

    /**
     * Creates a pending join request.
     *
     * @return the generated request id, or -1 when it could not be read
     */
    public int createRequest(int teamId, int userId, String message) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_REQUEST,
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.setString(3, message == null ? "" : message);
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
     * Loads the requests that were sent to one team.
     */
    public List<TeamRequest> findForTeam(int teamId) throws SQLException {
        List<TeamRequest> requests = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_PENDING)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRequest(rs));
                }
            }
        }
        return requests;
    }

    /**
     * Loads the requests that were sent to the teams the given user leads.
     *
     * @param leaderId the id of the team leader
     * @return the requests, never null
     */
    public List<TeamRequest> findForLeader(int leaderId) throws SQLException {
        List<TeamRequest> requests = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_FOR_LEADER)) {

            ps.setInt(1, leaderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRequest(rs));
                }
            }
        }
        return requests;
    }

    /**
     * Loads the requests that the given user sent to other teams.
     */
    public List<TeamRequest> findForUser(int userId) throws SQLException {
        List<TeamRequest> requests = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_FOR_USER)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapRequest(rs));
                }
            }
        }
        return requests;
    }

    /**
     * Loads one request.
     *
     * @return the request, or null when the id is unknown
     */    public TeamRequest findById(int requestId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ONE)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRequest(rs) : null;
            }
        }
    }

    /**
     * Changes the status of a request.
     */
    public boolean updateStatus(int requestId, String status) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {

            ps.setString(1, status);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Adds a user to the member list of a team.
     *
     * @return true when the user was added, false when the user was already a member
     */
    public boolean addMember(int teamId, int userId, String role) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_MEMBER)) {

            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return the status of the most recent request of this user for this team,
     *         or null when the user never requested to join
     */
    public String findExistingStatus(int teamId, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_EXISTING_REQUEST)) {

            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("status") : null;
            }
        }
    }

    private static TeamRequest mapRequest(ResultSet rs) throws SQLException {
        TeamRequest request = new TeamRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setTeamId(rs.getInt("team_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setMessage(rs.getString("message"));
        request.setStatus(rs.getString("status"));
        request.setRequestedAt(rs.getString("requested_at"));
        request.setTeamName(rs.getString("team_name"));
        request.setProjectTitle(rs.getString("project_title"));
        request.setRequesterName(rs.getString("full_name"));
        request.setRequesterEmail(rs.getString("email"));
        request.setRequesterSkills(rs.getString("skills"));
        return request;
    }
}
