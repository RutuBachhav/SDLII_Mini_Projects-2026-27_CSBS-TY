package com.splitease.rest;

import com.splitease.model.DashboardStats;
import com.splitease.service.StatsService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** GET /api/stats : numbers for the dashboard cards (consumed with jQuery AJAX). */
@Path("stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {

    private final StatsService service = new StatsService();

    @GET
    public DashboardStats stats() {
        return service.load();
    }
}
