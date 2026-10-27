package com.labloop.entity;

/** Lifecycle of a physical asset in a lab. */
public enum EquipmentStatus {
    AVAILABLE("Available"),
    IN_USE("In use"),
    UNDER_MAINTENANCE("Under maintenance"),
    RETIRED("Retired");

    private final String label;
    EquipmentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
