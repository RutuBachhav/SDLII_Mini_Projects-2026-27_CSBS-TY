package com.labloop.entity;

public enum TicketSeverity {
    LOW, MEDIUM, HIGH, CRITICAL;

    /** HIGH and CRITICAL faults take the equipment out of service immediately. */
    public boolean takesOutOfService() { return this == HIGH || this == CRITICAL; }
}
