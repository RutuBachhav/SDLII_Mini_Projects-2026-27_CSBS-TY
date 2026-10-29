package com.labloop.entity;

public enum UserRole {
    STUDENT("Student"),
    FACULTY("Faculty"),
    LAB_ASSISTANT("Lab Assistant"),
    ADMIN("Administrator");

    private final String label;
    UserRole(String label) { this.label = label; }
    public String getLabel() { return label; }

    /** Lab assistants and admins run the lab: approve, check out, maintain. */
    public boolean isStaff() { return this == LAB_ASSISTANT || this == ADMIN; }
}
