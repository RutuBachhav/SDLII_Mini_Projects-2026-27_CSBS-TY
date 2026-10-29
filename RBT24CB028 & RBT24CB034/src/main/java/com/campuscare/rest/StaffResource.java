package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.ComplaintResponse;
import com.campuscare.dto.StaffDto;
import com.campuscare.ejb.ComplaintServiceBean;
import com.campuscare.ejb.UserServiceBean;
import com.campuscare.security.AuthContext;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/staff")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffResource {

    @EJB
    private UserServiceBean userService;

    @EJB
    private ComplaintServiceBean complaintService;

    @GET
    public Response getAllStaff(@Context HttpHeaders headers) {
        AuthContext.requireUser(headers);
        List<StaffDto> staff = userService.getAllStaff();
        return Response.ok(ApiResponse.ok("Staff list retrieved", staff)).build();
    }

    @GET
    @Path("/{id}/complaints")
    public Response getStaffComplaints(@PathParam("id") Long id, @Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        List<ComplaintResponse> complaints = complaintService.getComplaintsByStaff(id);
        return Response.ok(ApiResponse.ok("Staff assigned complaints retrieved", complaints)).build();
    }
}
