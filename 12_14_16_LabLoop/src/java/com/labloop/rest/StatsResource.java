package com.labloop.rest;

import com.labloop.service.DashboardStats;
import com.labloop.service.LabMonitorBean;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** GET /api/stats - live numbers from the Singleton; the dashboard polls this every 30 seconds. */
@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class StatsResource {

    @EJB
    private LabMonitorBean monitor;

    @GET
    public DashboardStats stats() { return monitor.getStats(); }
}
