package com.labloop.rest;

import com.labloop.entity.Equipment;
import com.labloop.entity.Lab;
import com.labloop.service.EquipmentService;
import com.labloop.service.LabService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 *   GET /api/labs                 all labs
 *   GET /api/labs/{id}            one lab
 *   GET /api/labs/{id}/equipment  the One-to-Many side, served as its own endpoint
 *                                 (Lab.equipment is @JsonbTransient to avoid infinite JSON recursion)
 */
@Path("/labs")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class LabResource {

    @EJB
    private LabService labService;

    @EJB
    private EquipmentService equipmentService;

    @GET
    public List<Lab> list() { return labService.findAll(); }

    @GET
    @Path("/{id}")
    public Lab get(@PathParam("id") Long id) { return labService.find(id); }

    @GET
    @Path("/{id}/equipment")
    public List<Equipment> equipment(@PathParam("id") Long id) {
        labService.find(id);                     // 404 if the lab does not exist
        return equipmentService.search(id, null, null, null);
    }
}
