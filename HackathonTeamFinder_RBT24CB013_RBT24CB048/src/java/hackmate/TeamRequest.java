package hackmate;

/**
 * Simple representation of a row in the {@code team_requests} table.
 */
public class TeamRequest {

    /** Status of a join request. Matches the enum used in the database. */
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_REJECTED = "Rejected";

    private int requestId;
    private int teamId;
    private int userId;
    private String message;
    private String status;
    private String requestedAt;

    // Joined for display, they are not columns of team_requests.
    private String teamName;
    private String projectTitle;
    private String requesterName;
    private String requesterEmail;
    private String requesterSkills;

    public TeamRequest() {
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequesterSkills() {
        return requesterSkills;
    }

    public void setRequesterSkills(String requesterSkills) {
        this.requesterSkills = requesterSkills;
    }
}
