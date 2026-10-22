package com.service;

import java.io.Serializable;

/**
 * The dashboard's figures, all read from the database in one query by
 * {@code TaskDao.counts} and never cached.
 *
 * Nothing here is stored or incremented by hand, so creating, editing,
 * completing, reopening or deleting a task changes the next page load without
 * any code having to remember to adjust a counter.
 */
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long total;
    private final long completed;
    private final long inProgress;
    private final long pending;
    private final long overdue;
    private final long dueToday;

    public DashboardStats(long total, long completed, long inProgress,
            long pending, long overdue, long dueToday) {
        this.total = total;
        this.completed = completed;
        this.inProgress = inProgress;
        this.pending = pending;
        this.overdue = overdue;
        this.dueToday = dueToday;
    }

    public long getTotal() {
        return total;
    }

    public long getCompleted() {
        return completed;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getPending() {
        return pending;
    }

    public long getOverdue() {
        return overdue;
    }

    public long getDueToday() {
        return dueToday;
    }

    /** Everything not yet completed, whether pending or in progress. */
    public long getOpen() {
        return Math.max(0, total - completed);
    }

    /**
     * Completed / total * 100, rounded to a whole number. Zero when there are
     * no tasks at all, rather than a division by zero.
     */
    public int getCompletionPercent() {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round(completed * 100.0 / total);
    }

    public boolean isEmpty() {
        return total == 0;
    }
}
