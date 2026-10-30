package hackmate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for the {@code teams} and {@code team_members} tables.
 *
 * <p>Every statement is a prepared statement so user supplied text is never
 * concatenated into SQL.</p>
 */
public class TeamDAO {

    private static final String INSERT_TEAM =
            "INSERT INTO teams (team_name, project_title, project_description, required_skills, team_leader_id) "
          + "VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_MEMBER =
            "INSERT INTO team_members (team_id, user_id, role) VALUES (?, ?, ?)";

    private static final String TEAM_COLUMNS =
            "t.team_id, t.team_name, t.project_title, t.project_description, "
          + "t.required_skills, t.team_leader_id, u.full_name AS leader_name, "
          + "(SELECT COUNT(*) FROM team_members m WHERE m.team_id = t.team_id) AS member_count";

    /**
     * Creates a team and makes the creator its leader and first member.
     *
     * @return the generated team id, or -1 when it could not be read
     */
    public int createTeam(String teamName, String projectTitle, String description,
            String requiredSkills, int leaderId) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {

            int teamId;
            try (PreparedStatement ps = con.prepareStatement(INSERT_TEAM,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, teamName);
                ps.setString(2, projectTitle);
                ps.setString(3, description == null ? "" : description);
                ps.setString(4, requiredSkills == null ? "" : requiredSkills);
                ps.setInt(5, leaderId);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        return -1;
                    }
                    teamId = keys.getInt(1);
                }
            }

            // The creator becomes the leader and the first member of the team.
            try (PreparedStatement ps = con.prepareStatement(INSERT_MEMBER)) {
                ps.setInt(1, teamId);
                ps.setInt(2, leaderId);
                ps.setString(3, "Leader");
                ps.executeUpdate();
            }

            return teamId;
        }
    }

    /**
     * Loads every team, newest first, optionally filtered.
     *
     * @param skillFilter a skill to look for in the required skills, may be null
     * @param search      a text to look for in the name, title and description, may be null
     * @return the teams, never null
     */
    public List<Team> listTeams(String skillFilter, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT ").append(TEAM_COLUMNS)
                .append(" FROM teams t JOIN users u ON u.user_id = t.team_leader_id WHERE 1 = 1");

        List<Object> parameters = new ArrayList<>();

        if (skillFilter != null && !skillFilter.trim().isEmpty()) {
            sql.append(" AND t.required_skills LIKE ?");
            parameters.add("%" + skillFilter.trim() + "%");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (t.team_name LIKE ? OR t.project_title LIKE ? OR t.project_description LIKE ?)");
            String like = "%" + search.trim() + "%";
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
        }

        sql.append(" ORDER BY t.created_at DESC, t.team_id DESC");

        List<Team> teams = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teams.add(mapTeam(rs));
                }
            }
        }
        return teams;
    }

    /**
     * Loads one team.
     *
     * @return the team, or null when the id is unknown
     */
    public Team findById(int teamId) throws SQLException {
        String sql = "SELECT " + TEAM_COLUMNS
                + " FROM teams t JOIN users u ON u.user_id = t.team_leader_id "
                + "WHERE t.team_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTeam(rs) : null;
            }
        }
    }

    /**
     * Loads every team the given user leads or is a member of.
     *
     * @param userId the logged in user
     * @return the teams, never null
     */
    public List<Team> findTeamsForUser(int userId) throws SQLException {
        String sql = "SELECT " + TEAM_COLUMNS
                + " FROM teams t "
                + "JOIN team_members m ON m.team_id = t.team_id "
                + "JOIN users u ON u.user_id = t.team_leader_id "
                + "WHERE m.user_id = ? "
                + "ORDER BY t.created_at DESC";

        List<Team> teams = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teams.add(mapTeam(rs));
                }
            }
        }
        return teams;
    }

    /**
     * Loads the members of a team.
     *
     * @return the members, never null
     */
    public List<User> findMembers(int teamId) throws SQLException {
        String sql = "SELECT u.user_id, u.full_name, u.email, u.skills, m.role "
                + "FROM team_members m JOIN users u ON u.user_id = m.user_id "
                + "WHERE m.team_id = ? ORDER BY m.joined_at ASC";

        List<User> members = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User member = new User();
                    member.setUserId(rs.getInt("user_id"));
                    member.setFullName(rs.getString("full_name"));
                    member.setEmail(rs.getString("email"));
                    member.setSkills(rs.getString("skills"));
                    members.add(member);
                }
            }
        }
        return members;
    }

    /**
     * @return true when the user already belongs to the team
     */
    public boolean isMember(int teamId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM team_members WHERE team_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Counts the teams the given user leads.
     */
    public int countTeamsLed(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams WHERE team_leader_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static Team mapTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setTeamId(rs.getInt("team_id"));
        team.setTeamName(rs.getString("team_name"));
        team.setProjectTitle(rs.getString("project_title"));
        team.setProjectDescription(rs.getString("project_description"));
        team.setRequiredSkills(rs.getString("required_skills"));
        team.setTeamLeaderId(rs.getInt("team_leader_id"));
        team.setLeaderName(rs.getString("leader_name"));
        team.setMemberCount(rs.getString("member_count"));
        return team;
    }
}
