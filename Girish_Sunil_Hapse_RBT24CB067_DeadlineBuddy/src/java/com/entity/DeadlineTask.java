package com.entity;

import com.util.Dates;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * One piece of work with a deadline.
 *
 * Named {@code DeadlineTask} rather than {@code Task} so it does not collide
 * with {@code java.util.concurrent} or Ant's {@code Task} in anyone's imports.
 *
 * Two foreign keys point out of this row: the owner and the subject. The
 * composite key {@code (subject_id, user_id) -> subjects(id, user_id)} means the
 * database itself refuses a task whose subject belongs to a different student.
 *
 * The deadline buckets - overdue, due today, upcoming, completed - are computed
 * here from the status and the due date. Nothing about them is stored, so they
 * are right as soon as the date changes.
 */
@Entity
@Table(name = "tasks",
       indexes = {
           @Index(name = "ix_tasks_user_status", columnList = "user_id, status"),
           @Index(name = "ix_tasks_user_due", columnList = "user_id, due_date"),
           @Index(name = "ix_tasks_user_priority", columnList = "user_id, priority"),
           @Index(name = "ix_tasks_subject", columnList = "subject_id")
       })
@NamedQuery(name = "DeadlineTask.byIdAndUser",
            query = "SELECT t FROM DeadlineTask t JOIN FETCH t.subject "
                  + "WHERE t.id = :id AND t.user.id = :userId")
@NamedQuery(name = "DeadlineTask.countBySubject",
            query = "SELECT COUNT(t) FROM DeadlineTask t WHERE t.subject.id = :subjectId")
@NamedQuery(name = "DeadlineTask.upcomingByUser",
            query = "SELECT t FROM DeadlineTask t JOIN FETCH t.subject "
                  + "WHERE t.user.id = :userId AND t.status <> com.entity.TaskStatus.COMPLETED "
                  + "AND t.dueDate >= :from ORDER BY t.dueDate, t.dueTime, t.id")
public class DeadlineTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** A task due within this many days counts as urgent in the UI. */
    private static final int URGENT_WINDOW_DAYS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tasks_user"))
    private User user;

    /**
     * Eager because every task that is listed is listed with its subject's name
     * and colour; fetching it lazily would be a query per row.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_tasks_subject_owner"))
    private Subject subject;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 16)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 8)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private TaskStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "due_time")
    private LocalTime dueTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public DeadlineTask() {
    }

    @PrePersist
    void onInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (taskType == null) {
            taskType = TaskType.OTHER;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -- derived deadline state ---------------------------------------------

    /**
     * The task's deadline bucket. A completed task is COMPLETED and nothing
     * else - finishing late does not leave it counted as overdue.
     */
    @Transient
    public DeadlineCategory getCategory() {
        if (status == TaskStatus.COMPLETED) {
            return DeadlineCategory.COMPLETED;
        }
        if (dueDate == null) {
            return DeadlineCategory.UPCOMING;
        }
        LocalDate today = Dates.today();
        if (dueDate.isBefore(today)) {
            return DeadlineCategory.OVERDUE;
        }
        if (dueDate.isEqual(today)) {
            return DeadlineCategory.DUE_TODAY;
        }
        return DeadlineCategory.UPCOMING;
    }

    @Transient
    public boolean isOverdue() {
        return getCategory() == DeadlineCategory.OVERDUE;
    }

    @Transient
    public boolean isDueToday() {
        return getCategory() == DeadlineCategory.DUE_TODAY;
    }

    @Transient
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    /** Overdue, or due within the next couple of days. */
    @Transient
    public boolean isUrgent() {
        DeadlineCategory category = getCategory();
        if (category == DeadlineCategory.COMPLETED) {
            return false;
        }
        return category == DeadlineCategory.OVERDUE
                || Dates.daysFromToday(dueDate) <= URGENT_WINDOW_DAYS;
    }

    /** Whole days until the deadline; negative once it has passed. */
    @Transient
    public long getDaysUntilDue() {
        return Dates.daysFromToday(dueDate);
    }

    /** "due today", "due in 4 days", "3 days ago" - the list subtitle. */
    @Transient
    public String getDueLabel() {
        if (dueDate == null) {
            return "";
        }
        if (status == TaskStatus.COMPLETED) {
            // The row already prints the due date next to this label, so say
            // something it does not already say: when it was finished.
            return completedAt == null
                    ? "completed"
                    : "completed " + Dates.formatDate(completedAt.toLocalDate());
        }
        long days = Dates.daysFromToday(dueDate);
        if (days < 0) {
            return "overdue by " + Math.abs(days) + (Math.abs(days) == 1 ? " day" : " days");
        }
        return "due " + Dates.relative(dueDate);
    }

    /** "14 Oct 2026" - formatted once here so the views hold no date logic. */
    @Transient
    public String getDueDateDisplay() {
        return Dates.formatDate(dueDate);
    }

    @Transient
    public String getCreatedAtDisplay() {
        return Dates.formatDateTime(createdAt);
    }

    @Transient
    public String getUpdatedAtDisplay() {
        return Dates.formatDateTime(updatedAt);
    }

    @Transient
    public String getCompletedAtDisplay() {
        return Dates.formatDateTime(completedAt);
    }

    @Transient
    public String getDueDateIso() {
        return dueDate == null ? "" : dueDate.format(Dates.HTML_DATE);
    }

    @Transient
    public String getDueTimeIso() {
        return dueTime == null ? "" : dueTime.format(Dates.HTML_TIME);
    }

    // -- accessors ----------------------------------------------------------

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeadlineTask)) {
            return false;
        }
        DeadlineTask that = (DeadlineTask) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "DeadlineTask{id=" + id + ", title=" + title + ", status=" + status + "}";
    }
}
