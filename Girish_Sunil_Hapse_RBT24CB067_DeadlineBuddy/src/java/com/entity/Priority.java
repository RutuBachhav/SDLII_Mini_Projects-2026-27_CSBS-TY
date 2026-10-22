package com.entity;

/**
 * How urgent a task is, independent of when it is due.
 *
 * {@code rank} orders HIGH before MEDIUM before LOW; the queries sort with a
 * JPQL CASE expression that uses the same order.
 */
public enum Priority {

    HIGH("High", 0, "priority-high"),
    MEDIUM("Medium", 1, "priority-medium"),
    LOW("Low", 2, "priority-low");

    private final String label;
    private final int rank;
    private final String cssClass;

    Priority(String label, int rank, String cssClass) {
        this.label = label;
        this.rank = rank;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public int getRank() {
        return rank;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static Priority parse(String raw) {
        if (raw == null) {
            return null;
        }
        for (Priority p : values()) {
            if (p.name().equalsIgnoreCase(raw.trim())) {
                return p;
            }
        }
        return null;
    }
}
