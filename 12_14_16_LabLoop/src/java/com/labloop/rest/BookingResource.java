package com.labloop.rest;

import com.labloop.cdi.UserSession;
import com.labloop.entity.Booking;
import com.labloop.entity.BookingStatus;
import com.labloop.service.BookingService;
import com.labloop.util.LabLoopException;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 *   GET /api/bookings?status=PENDING   (staff: everyone's bookings, others: only their own)
 *   GET /api/bookings/{id}
 *   GET /api/bookings/overdue          items not returned on time
 */
@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class BookingResource {

    @EJB
    private BookingService bookingService;

    @Inject
    private UserSession me;

    @GET
    public List<Booking> list(@QueryParam("status") String status) {
        requireLogin();
        if (!me.isStaff()) {
            return bookingService.forUser(me.getUserId());
        }
        return bookingService.byStatus(EquipmentResource.parse(BookingStatus.class, status));
    }

    @GET
    @Path("/overdue")
    public List<Booking> overdue() {
        requireLogin();
        return bookingService.overdue();
    }

    @GET
    @Path("/{id}")
    public Booking get(@PathParam("id") Long id) {
        requireLogin();
        Booking b = bookingService.find(id);
        if (!me.isStaff() && !b.getRequestedBy().getId().equals(me.getUserId())) {
            throw LabLoopException.forbidden("This booking belongs to someone else.");
        }
        return b;
    }

    private void requireLogin() {
        if (!me.isLoggedIn()) {
            throw LabLoopException.forbidden("Log in to LabLoop to see bookings.");
        }
    }
}
