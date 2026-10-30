package hackmate;

/**
 * Simple representation of a row in the {@code teams} table.
 */
public class Team {

    private int teamId;
    private String teamName;
    private String projectTitle;
    private String projectDescription;
    private String requiredSkills;
    private int teamLeaderId;
    private String leaderName;
    private String memberCount;

    public Team() {
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
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

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int getTeamLeaderId() {
        return teamLeaderId;
    }

    public void setTeamLeaderId(int teamLeaderId) {
        this.teamLeaderId = teamLeaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    /**
     * Splits the comma separated required skill list into individual skills.
     *
     * @return the skills, never null
     */
    public String[] getSkillList() {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return new String[0];
        }
        String[] parts = requiredSkills.split(",");
        int count = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                count++;
            }
        }
        String[] result = new String[count];
        int index = 0;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                result[index++] = part.trim();
            }
        }
        return result;
    }
}
