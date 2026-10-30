package hackmate;

/**
 * Simple representation of a row in the {@code users} table.
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String skills;

    public User() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    /**
     * Splits the comma separated skill list into individual trimmed skills.
     *
     * @return the skills, never null
     */
    public String[] getSkillList() {
        if (skills == null || skills.trim().isEmpty()) {
            return new String[0];
        }
        String[] parts = skills.split(",");
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
