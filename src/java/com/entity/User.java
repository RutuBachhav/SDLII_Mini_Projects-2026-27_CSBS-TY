package com.entity;

import com.util.Dates;
import java.time.LocalDateTime;

/** A registered account. The password hash never leaves the data layer. */
public class User {

    private int id;
    private String name;
    private String email;
    private String role = "USER";
    private boolean active = true;
    private LocalDateTime createdAt;
    private int reportCount;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    public String getCreatedLabel() { return Dates.day(createdAt == null ? null : createdAt.toLocalDate()); }
}
