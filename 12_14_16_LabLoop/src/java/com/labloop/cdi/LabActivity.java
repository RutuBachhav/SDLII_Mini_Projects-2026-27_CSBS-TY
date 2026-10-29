package com.labloop.cdi;

import java.io.Serializable;

/**
 * CDI EVENT payload. Services call  activity.fire(new LabActivity(...))  and any method
 * with  @Observes LabActivity  receives it - the services never know who is listening.
 * Listeners: ActivityLogService (writes audit table) and LabMonitorBean (refreshes dashboard).
 */
public class LabActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String actor;
    private final String action;
    private final String details;

    public LabActivity(String actor, String action, String details) {
        this.actor = actor;
        this.action = action;
        this.details = details;
    }

    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }

    @Override
    public String toString() { return actor + " " + action + " - " + details; }
}
