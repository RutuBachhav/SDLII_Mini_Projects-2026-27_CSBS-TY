package com.pms.model;

/**
 * Authenticated user identity kept in the HTTP session (never the password
 * hash). Plain JavaBean for consistency with the other EL-rendered models.
 */
public final class User {

    private final long userId;
    private final String email;
    private final String role;

    public User(long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
