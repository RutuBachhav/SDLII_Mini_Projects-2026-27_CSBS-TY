package com.labloop.cdi;

import com.labloop.entity.LabUser;
import com.labloop.entity.UserRole;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * CDI bean with @SessionScoped: one instance per logged-in browser session.
 * Servlets, the filter and REST resources get it with @Inject - nobody calls "new UserSession()".
 */
@SessionScoped
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String fullName;
    private String email;
    private UserRole role;

    public void login(LabUser user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    public void logout() {
        userId = null;
        fullName = null;
        email = null;
        role = null;
    }

    public boolean isLoggedIn() { return userId != null; }
    public boolean isStaff() { return role != null && role.isStaff(); }
    public boolean isAdmin() { return role == UserRole.ADMIN; }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getRoleLabel() { return role == null ? "" : role.getLabel(); }
    public String getFirstName() {
        if (fullName == null) return "";
        String[] p = fullName.replace("Dr. ", "").trim().split("\\s+");
        return p[0];
    }
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] p = fullName.replace("Dr. ", "").trim().split("\\s+");
        return (p[0].substring(0, 1) + (p.length > 1 ? p[p.length - 1].substring(0, 1) : "")).toUpperCase();
    }
}
