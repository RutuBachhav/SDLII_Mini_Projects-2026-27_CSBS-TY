package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.ComplaintResponse;
import com.campuscare.dto.StudentDto;
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

@Path("/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {

    @EJB
    private UserServiceBean userService;

    @EJB
    private ComplaintServiceBean complaintService;

    @GET
    public Response getAllStudents(@Context HttpHeaders headers) {
        AuthContext.requireRole(headers, "ADMIN", "STAFF");
        List<StudentDto> students = userService.getAllStudents();
        return Response.ok(ApiResponse.ok("Students retrieved", students)).build();
    }

    @GET
    @Path("/{id}/complaints")
    public Response getStudentComplaints(@PathParam("id") Long id, @Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        List<ComplaintResponse> complaints = complaintService.getComplaintsByStudent(id);
        return Response.ok(ApiResponse.ok("Student complaints retrieved", complaints)).build();
    }
}
