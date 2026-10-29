package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.DepartmentDto;
import com.campuscare.ejb.DepartmentServiceBean;
import com.campuscare.security.AuthContext;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DepartmentResource {

    @EJB
    private DepartmentServiceBean departmentService;

    @GET
    public Response getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return Response.ok(ApiResponse.ok("Departments retrieved", departments)).build();
    }

    @GET
    @Path("/{id}")
    public Response getDepartmentById(@PathParam("id") Long id) {
        DepartmentDto department = departmentService.getDepartmentById(id);
        return Response.ok(ApiResponse.ok("Department details", department)).build();
    }

    @POST
    public Response createDepartment(@Valid DepartmentDto dto, @Context HttpHeaders headers) {
        AuthContext.requireRole(headers, "ADMIN");
        DepartmentDto created = departmentService.createDepartment(dto);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok("Department created successfully", created))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateDepartment(@PathParam("id") Long id, @Valid DepartmentDto dto, @Context HttpHeaders headers) {
        AuthContext.requireRole(headers, "ADMIN");
        DepartmentDto updated = departmentService.updateDepartment(id, dto);
        return Response.ok(ApiResponse.ok("Department updated successfully", updated)).build();
    }
}
