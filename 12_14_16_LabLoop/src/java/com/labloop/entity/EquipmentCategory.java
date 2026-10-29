package com.labloop.entity;

public enum EquipmentCategory {
    COMPUTING("Computing"),
    ELECTRONICS("Electronics & Test"),
    NETWORKING("Networking"),
    ROBOTICS("Robotics"),
    IOT("IoT & Embedded"),
    FABRICATION("Fabrication");

    private final String label;
    EquipmentCategory(String label) { this.label = label; }
    public String getLabel() { return label; }
}
