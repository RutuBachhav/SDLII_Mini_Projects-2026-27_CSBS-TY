package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.DashboardStatsDto;
import com.campuscare.ejb.DashboardServiceBean;
import com.campuscare.ejb.UserServiceBean;
import com.campuscare.entity.Staff;
import com.campuscare.entity.Student;
import com.campuscare.security.AuthContext;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardResource {

    @EJB
    private DashboardServiceBean dashboardService;

    @EJB
    private UserServiceBean userService;

    @GET
    public Response getDashboard(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        if ("ADMIN".equals(claims.getRole())) {
            return Response.ok(ApiResponse.ok("Admin dashboard statistics", dashboardService.getAdminDashboardStats())).build();
        } else if ("STAFF".equals(claims.getRole())) {
            Staff staff = userService.getStaffEntityByUserId(claims.getUserId());
            Long staffId = staff != null ? staff.getId() : -1L;
            return Response.ok(ApiResponse.ok("Staff dashboard statistics", dashboardService.getStaffDashboardStats(staffId))).build();
        } else {
            Student student = userService.getStudentEntityByUserId(claims.getUserId());
            Long studentId = student != null ? student.getId() : -1L;
            return Response.ok(ApiResponse.ok("Student dashboard statistics", dashboardService.getStudentDashboardStats(studentId))).build();
        }
    }

    @GET
    @Path("/admin")
    public Response getAdminDashboard(@Context HttpHeaders headers) {
        AuthContext.requireRole(headers, "ADMIN");
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        return Response.ok(ApiResponse.ok("Admin dashboard statistics", stats)).build();
    }

    @GET
    @Path("/staff")
    public Response getStaffDashboard(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireRole(headers, "STAFF");
        Staff staff = userService.getStaffEntityByUserId(claims.getUserId());
        if (staff == null) {
            return Response.ok(ApiResponse.ok("Staff dashboard statistics", new DashboardStatsDto())).build();
        }
        DashboardStatsDto stats = dashboardService.getStaffDashboardStats(staff.getId());
        return Response.ok(ApiResponse.ok("Staff dashboard statistics", stats)).build();
    }

    @GET
    @Path("/student")
    public Response getStudentDashboard(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireRole(headers, "STUDENT");
        Student student = userService.getStudentEntityByUserId(claims.getUserId());
        if (student == null) {
            return Response.ok(ApiResponse.ok("Student dashboard statistics", new DashboardStatsDto())).build();
        }
        DashboardStatsDto stats = dashboardService.getStudentDashboardStats(student.getId());
        return Response.ok(ApiResponse.ok("Student dashboard statistics", stats)).build();
    }

    @GET
    @Path("/students")
    public Response getStudentsDashboard(@Context HttpHeaders headers) {
        return getStudentDashboard(headers);
    }
}
