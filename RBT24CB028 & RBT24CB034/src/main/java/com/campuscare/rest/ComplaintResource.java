package com.campuscare.rest;

import com.campuscare.dto.*;
import com.campuscare.ejb.ComplaintServiceBean;
import com.campuscare.security.AuthContext;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/complaints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ComplaintResource {

    @EJB
    private ComplaintServiceBean complaintService;

    @GET
    public Response getAllComplaints(
            @QueryParam("status") String status,
            @QueryParam("category") String category,
            @QueryParam("priority") String priority,
            @QueryParam("departmentId") Long departmentId,
            @Context HttpHeaders headers) {

        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        List<ComplaintResponse> list = complaintService.getAllComplaints(status, category, priority, departmentId);
        return Response.ok(ApiResponse.ok("Complaints retrieved", list)).build();
    }

    @GET
    @Path("/{id}")
    public Response getComplaintById(@PathParam("id") Long id, @Context HttpHeaders headers) {
        AuthContext.requireUser(headers);
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        return Response.ok(ApiResponse.ok("Complaint details", complaint)).build();
    }

    @POST
    public Response submitComplaint(@Valid ComplaintRequest request, @Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireRole(headers, "STUDENT");
        ComplaintResponse created = complaintService.createComplaint(claims.getUserId(), request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok("Complaint submitted successfully", created))
                .build();
    }

    @PUT
    @Path("/{id}/assign")
    public Response assignComplaint(
            @PathParam("id") Long id,
            @Valid AssignRequest request,
            @Context HttpHeaders headers) {

        TokenUtil.TokenClaims claims = AuthContext.requireRole(headers, "ADMIN");
        ComplaintResponse updated = complaintService.assignComplaint(id, request.getStaffId(), request.getPriority(), claims.getUserId());
        return Response.ok(ApiResponse.ok("Complaint assigned successfully", updated)).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(
            @PathParam("id") Long id,
            @Valid StatusChangeRequest request,
            @Context HttpHeaders headers) {

        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        ComplaintResponse updated = complaintService.updateComplaintStatus(
                id, request.getStatus(), request.getRemarks(), claims.getUserId(), claims.getRole());
        return Response.ok(ApiResponse.ok("Status updated successfully", updated)).build();
    }

    @POST
    @Path("/{id}/updates")
    public Response addUpdate(
            @PathParam("id") Long id,
            @Valid ComplaintUpdateRequest request,
            @Context HttpHeaders headers) {

        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        ComplaintResponse updated = complaintService.addComplaintUpdate(
                id, request, claims.getUserId(), claims.getRole());
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok("Update added successfully", updated))
                .build();
    }

    @GET
    @Path("/{id}/updates")
    public Response getUpdates(@PathParam("id") Long id, @Context HttpHeaders headers) {
        AuthContext.requireUser(headers);
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        return Response.ok(ApiResponse.ok("Complaint updates retrieved", complaint.getUpdates())).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteComplaint(@PathParam("id") Long id, @Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        complaintService.deleteComplaint(id, claims.getUserId(), claims.getRole());
        return Response.ok(ApiResponse.ok("Complaint deleted successfully")).build();
    }
}
