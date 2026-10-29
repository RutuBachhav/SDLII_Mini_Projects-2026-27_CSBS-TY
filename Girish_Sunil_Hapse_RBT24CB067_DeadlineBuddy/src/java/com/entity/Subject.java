package com.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A course a student is tracking work for - "Operating Systems", "DBMS".
 *
 * A subject belongs to exactly one user and its name is unique within that
 * user, so two students can both have a subject called "DBMS" while neither can
 * create it twice.
 *
 * {@code taskCount} and {@code completedCount} are not columns. They are filled
 * in by {@code SubjectDao.findWithCounts} from a single grouped query so the
 * subject cards do not trigger a query per row.
 */
@Entity
@Table(name = "subjects",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_subjects_user_name", columnNames = {"user_id", "name"}),
           @UniqueConstraint(name = "uq_subjects_id_user", columnNames = {"id", "user_id"})
       },
       indexes = @Index(name = "ix_subjects_user", columnList = "user_id"))
@NamedQuery(name = "Subject.byUser",
            query = "SELECT s FROM Subject s WHERE s.user.id = :userId ORDER BY s.name")
@NamedQuery(name = "Subject.byIdAndUser",
            query = "SELECT s FROM Subject s WHERE s.id = :id AND s.user.id = :userId")
public class Subject implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Used when the user does not pick a colour. */
    public static final String DEFAULT_COLOR = "#64748b";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @jakarta.persistence.ForeignKey(name = "fk_subjects_user"))
    private User user;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private long taskCount;

    @Transient
    private long completedCount;

    public Subject() {
    }

    public Subject(User user, String name, String color) {
        this.user = user;
        this.name = name;
        this.color = color;
    }

    @PrePersist
    void onInsert() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    /** Never null - the JSPs use this for the swatch. */
    public String getDisplayColor() {
        return color == null || color.trim().isEmpty() ? DEFAULT_COLOR : color;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getPendingCount() {
        return Math.max(0, taskCount - completedCount);
    }

    /** Completed / total * 100, and 0 when there is nothing to divide by. */
    public int getProgressPercent() {
        if (taskCount <= 0) {
            return 0;
        }
        return (int) Math.round(completedCount * 100.0 / taskCount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Subject)) {
            return false;
        }
        Subject that = (Subject) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Subject{id=" + id + ", name=" + name + "}";
    }
}
