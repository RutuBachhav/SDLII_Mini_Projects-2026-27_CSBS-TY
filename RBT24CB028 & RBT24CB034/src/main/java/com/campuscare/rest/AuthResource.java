package com.campuscare.rest;

import com.campuscare.dto.ApiResponse;
import com.campuscare.dto.AuthResponse;
import com.campuscare.dto.LoginRequest;
import com.campuscare.dto.RegisterRequest;
import com.campuscare.ejb.UserServiceBean;
import com.campuscare.entity.Staff;
import com.campuscare.entity.Student;
import com.campuscare.entity.User;
import com.campuscare.exception.AppException;
import com.campuscare.security.AuthContext;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @EJB
    private UserServiceBean userService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        AuthResponse auth = userService.login(request);
        return Response.ok(ApiResponse.ok("Login successful", auth)).build();
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        AuthResponse auth = userService.register(request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.ok("Registration successful", auth))
                .build();
    }

    @GET
    @Path("/me")
    public Response me(@Context HttpHeaders headers) {
        TokenUtil.TokenClaims claims = AuthContext.requireUser(headers);
        User user = userService.getUserById(claims.getUserId());
        if (user == null) {
            throw new AppException("User not found", Response.Status.NOT_FOUND);
        }

        AuthResponse resp = new AuthResponse(
                null,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone()
        );

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            Student s = userService.getStudentEntityByUserId(user.getId());
            if (s != null) {
                resp.setStudentId(s.getId());
            }
        } else if ("STAFF".equalsIgnoreCase(user.getRole())) {
            Staff st = userService.getStaffEntityByUserId(user.getId());
            if (st != null) {
                resp.setStaffId(st.getId());
                if (st.getDepartment() != null) {
                    resp.setDepartmentId(st.getDepartment().getId());
                    resp.setDepartmentName(st.getDepartment().getDepartmentName());
                }
            }
        }

        return Response.ok(ApiResponse.ok("User details", resp)).build();
    }
}
