package com.labloop.rest;

import com.labloop.cdi.UserSession;
import com.labloop.entity.*;
import com.labloop.service.BookingService;
import com.labloop.service.EquipmentService;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS resource - full CRUD over HTTP:
 *   GET    /api/equipment                     list (filters: ?labId=1&status=AVAILABLE&category=IOT&q=pi)
 *   GET    /api/equipment/{id}                one item
 *   GET    /api/equipment/tag/{assetTag}      look up by the sticker on the device (QR / barcode scanner)
 *   GET    /api/equipment/{id}/availability   taken slots for a day (?date=2026-10-13)
 *   POST   /api/equipment                     add      (staff login required)
 *   PUT    /api/equipment/{id}                update   (staff login required)
 *   DELETE /api/equipment/{id}                delete   (staff login required)
 * Reads are public (a lab notice-board screen can show availability). Writes need a staff session.
 */
@Path("/equipment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class EquipmentResource {

    @EJB
    private EquipmentService equipmentService;

    @EJB
    private BookingService bookingService;

    @Inject
    private UserSession me;

    @GET
    public List<Equipment> list(@QueryParam("labId") Long labId,
                                @QueryParam("status") String status,
                                @QueryParam("category") String category,
                                @QueryParam("q") String q) {
        return equipmentService.search(labId, parse(EquipmentStatus.class, status),
                parse(EquipmentCategory.class, category), q);
    }

    @GET
    @Path("/{id}")
    public Equipment get(@PathParam("id") Long id) {
        return equipmentService.find(id);    // throws NOT_FOUND -> mapper returns 404 JSON
    }

    @GET
    @Path("/tag/{tag}")
    public Equipment byTag(@PathParam("tag") String tag) {
        Equipment e = equipmentService.findByTag(tag);
        if (e == null) throw LabLoopException.notFound("No equipment with asset tag " + tag);
        return e;
    }

    @GET
    @Path("/{id}/availability")
    public Map<String, Object> availability(@PathParam("id") Long id, @QueryParam("date") String date) {
        Equipment e = equipmentService.find(id);
        LocalDate day;
        try {
            day = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw LabLoopException.validation("date must be in yyyy-MM-dd format");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("equipmentId", e.getId());
        out.put("assetTag", e.getAssetTag());
        out.put("status", e.getStatus());
        out.put("bookable", e.isBookable());
        out.put("date", day.toString());
        out.put("taken", bookingService.takenOn(id, day).stream().map(b -> {
            Map<String, Object> slot = new LinkedHashMap<>();
            slot.put("bookingId", b.getId());
            slot.put("start", b.getStartTime().toString());
            slot.put("end", b.getEndTime().toString());
            slot.put("status", b.getStatus());
            return slot;
        }).toList());
        return out;
    }

    @POST
    public Response create(EquipmentRequest body) {
        requireStaff();
        if (body == null) throw LabLoopException.validation("Request body is required");
        Equipment saved = equipmentService.create(body.toEntity(), body.getLabId(), me.getFullName() + " (API)");
        return Response.created(UriBuilder.fromPath("/api/equipment/{id}").build(saved.getId()))
                .entity(saved).build();                                      // 201 Created
    }

    @PUT
    @Path("/{id}")
    public Equipment update(@PathParam("id") Long id, EquipmentRequest body) {
        requireStaff();
        if (body == null) throw LabLoopException.validation("Request body is required");
        return equipmentService.update(id, body.toEntity(), body.getLabId(), me.getFullName() + " (API)");
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        requireStaff();
        equipmentService.delete(id, me.getFullName() + " (API)");
        return Response.noContent().build();                                 // 204 No Content
    }

    private void requireStaff() {
        if (!me.isLoggedIn()) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiError(401, "UNAUTHORIZED", List.of("Log in to LabLoop first.")))
                    .build());
        }
        if (!me.isStaff()) {
            throw LabLoopException.forbidden("Only lab assistants and admins can change inventory.");
        }
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw LabLoopException.validation("Unknown " + type.getSimpleName() + ": " + value);
        }
    }
}
