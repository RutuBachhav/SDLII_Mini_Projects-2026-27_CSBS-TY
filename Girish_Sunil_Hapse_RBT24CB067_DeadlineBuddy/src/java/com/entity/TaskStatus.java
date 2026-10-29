package com.entity;

/**
 * Where a task is in its small state machine.
 *
 * <pre>
 *   PENDING  &lt;-&gt;  IN_PROGRESS
 *      \             /
 *       v           v
 *        COMPLETED  --(reopen)--&gt;  PENDING
 * </pre>
 *
 * {@link #canMoveTo} is the single definition of a legal transition; the
 * service layer asks it before writing, and the write itself is a conditional
 * UPDATE on the current status so two concurrent requests cannot both win.
 */
public enum TaskStatus {

    PENDING("Pending", "status-pending"),
    IN_PROGRESS("In Progress", "status-progress"),
    COMPLETED("Completed", "status-completed");

    private final String label;
    private final String cssClass;

    TaskStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    /** True when a task may move from this status to {@code target}. */
    public boolean canMoveTo(TaskStatus target) {
        if (target == null || target == this) {
            return false;
        }
        switch (this) {
            case PENDING:
                return target == IN_PROGRESS || target == COMPLETED;
            case IN_PROGRESS:
                return target == PENDING || target == COMPLETED;
            case COMPLETED:
                // Reopening always returns a task to the start of the machine.
                return target == PENDING;
            default:
                return false;
        }
    }

    public static TaskStatus parse(String raw) {
        if (raw == null) {
            return null;
        }
        for (TaskStatus s : values()) {
            if (s.name().equalsIgnoreCase(raw.trim())) {
                return s;
            }
        }
        return null;
    }
}
