package com.entity;

/**
 * What kind of work a deadline is. Stored as the enum name (a VARCHAR), not its
 * ordinal, so inserting a new type later cannot silently reinterpret old rows.
 */
public enum TaskType {

    ASSIGNMENT("Assignment"),
    PRACTICAL("Practical"),
    SUBMISSION("Submission"),
    EXAM("Exam"),
    OTHER("Other");

    private final String label;

    TaskType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Parses a form value; null or unknown input gives null, never an error. */
    public static TaskType parse(String raw) {
        if (raw == null) {
            return null;
        }
        for (TaskType t : values()) {
            if (t.name().equalsIgnoreCase(raw.trim())) {
                return t;
            }
        }
        return null;
    }
}
