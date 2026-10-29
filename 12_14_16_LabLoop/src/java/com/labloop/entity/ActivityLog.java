package com.labloop.entity;

import com.labloop.util.Fmt;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Audit trail row. Written automatically by a CDI event observer (see ActivityLogService). */
@Entity
@Table(name = "ACTIVITY_LOG")
public class ActivityLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 60)
    private String actor;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(length = 300)
    private String details;

    public ActivityLog() { }

    public ActivityLog(String actor, String action, String details) {
        this.occurredAt = LocalDateTime.now();
        this.actor = actor;
        this.action = action;
        this.details = details;
    }

    public String getOccurredDisplay() { return Fmt.dateTime(occurredAt); }

    public Long getId() { return id; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}
