package com.labloop.entity;

public enum TicketStatus {
    OPEN("Open"), IN_PROGRESS("In progress"), RESOLVED("Resolved");

    private final String label;
    TicketStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
