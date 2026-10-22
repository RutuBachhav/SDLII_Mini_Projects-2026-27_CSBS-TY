package com.entity;

/**
 * Which deadline bucket a task falls into. Derived from status and due date
 * every time it is asked for, never stored - so the buckets are correct the
 * moment the date rolls over, with no scheduled job to refresh them.
 */
public enum DeadlineCategory {

    OVERDUE("Overdue", "cat-overdue"),
    DUE_TODAY("Due today", "cat-today"),
    UPCOMING("Upcoming", "cat-upcoming"),
    COMPLETED("Completed", "cat-completed");

    private final String label;
    private final String cssClass;

    DeadlineCategory(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }
}
