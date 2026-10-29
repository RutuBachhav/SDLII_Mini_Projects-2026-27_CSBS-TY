package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.NotificationDto;
import com.campuscare.ejb.NotificationServiceBean;
import com.campuscare.security.AuthContext;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @EJB
    private NotificationServiceBean notificationService;

    @GET
    public Response getMyNotifications(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        List<NotificationDto> notifications = notificationService.getUserNotifications(claims.getUserId());
        return Response.ok(ApiResponse.ok("Notifications retrieved", notifications)).build();
    }

    @PUT
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") Long id, @Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        boolean success = notificationService.markAsRead(id, claims.getUserId());
        return Response.ok(ApiResponse.ok("Notification updated", success)).build();
    }

    @PUT
    @Path("/read-all")
    public Response markAllAsRead(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        notificationService.markAllAsRead(claims.getUserId());
        return Response.ok(ApiResponse.ok("All notifications marked as read")).build();
    }
}
