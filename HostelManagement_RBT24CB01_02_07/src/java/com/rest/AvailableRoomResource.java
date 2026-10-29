package com.rest;

import com.room.AvailableRoom;
import com.room.RoomRequestDAO;
import com.security.AuthorizationService;
import com.security.Permission;
import com.security.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;

@Path("/available-rooms")
@Produces(MediaType.APPLICATION_JSON)
public class AvailableRoomResource {

    private final RoomRequestDAO rooms = new RoomRequestDAO();

    @GET
    public Response search(
            @Context HttpServletRequest request,
            @QueryParam("hostelId") String hostelId,
            @QueryParam("minFee") String minFee,
            @QueryParam("maxFee") String maxFee,
            @QueryParam("minCapacity") String minCapacity,
            @QueryParam("roomNumber") String roomNumber,
            @QueryParam("availableOnly") String availableOnly) {

        if (!AuthorizationService.allowsAny(
                SessionAuth.roles(request.getSession(false)),
                Permission.ROOM_SEARCH)) {
            return json(Response.Status.FORBIDDEN,
                    "{\"error\":\"Forbidden\","
                    + "\"message\":\"ROOM_SEARCH permission required\"}");
        }

        Integer parsedHostel;
        Double parsedMinFee;
        Double parsedMaxFee;
        Integer parsedCapacity;
        boolean parsedAvailableOnly;

        try {
            parsedHostel = parseInteger(hostelId, 1, "hostelId");
            parsedMinFee = parseDouble(minFee, 0, "minFee");
            parsedMaxFee = parseDouble(maxFee, 0, "maxFee");
            parsedCapacity = parseInteger(minCapacity, 1, "minCapacity");
            parsedAvailableOnly = parseAvailableOnly(availableOnly);
        } catch (IllegalArgumentException ex) {
            return json(Response.Status.BAD_REQUEST,
                    "{\"error\":\"Bad Request\",\"message\":\""
                    + ex.getMessage() + "\"}");
        }

        if (roomNumber != null && roomNumber.trim().length() > 50) {
            return json(Response.Status.BAD_REQUEST,
                    "{\"error\":\"Bad Request\",\"message\":\"roomNumber "
                    + "must be 50 characters or fewer\"}");
        }
        if (parsedMinFee != null && parsedMaxFee != null
                && parsedMinFee > parsedMaxFee) {
            return json(Response.Status.BAD_REQUEST,
                    "{\"error\":\"Bad Request\",\"message\":\"minFee cannot "
                    + "be greater than maxFee\"}");
        }

        List<AvailableRoom> results = rooms.search(parsedHostel,
                parsedMinFee, parsedMaxFee, parsedCapacity,
                roomNumber == null ? null : roomNumber.trim(),
                parsedAvailableOnly);
        return Response.ok(results).build();
    }

    private Response json(Response.Status status, String body) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private Integer parseInteger(String value, int min, String name) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min) {
                throw new IllegalArgumentException(
                        name + " must be at least " + min);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a whole number");
        }
    }

    private Double parseDouble(String value, double min, String name) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            if (parsed < min) {
                throw new IllegalArgumentException(
                        name + " must be at least " + min);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a number");
        }
    }

    private boolean parseAvailableOnly(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String clean = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(clean)) {
            return true;
        }
        if ("false".equals(clean)) {
            return false;
        }
        throw new IllegalArgumentException(
                "availableOnly must be true or false");
    }
}
