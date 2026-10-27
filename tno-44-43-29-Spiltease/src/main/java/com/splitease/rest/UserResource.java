package com.splitease.rest;

import com.splitease.model.User;
import com.splitease.service.UserService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/** REST endpoints for people:  /api/users */
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService service = new UserService();

    /** GET /api/users : list everyone */
    @GET
    public List<User> list() {
        return service.listUsers();
    }

    /** GET /api/users/5 */
    @GET
    @Path("{id}")
    public User get(@PathParam("id") int id) {
        return service.getUser(id);
    }

    /** POST /api/users  {"name":"..","email":"..","phone":".."} */
    @POST
    public User create(User user) {
        return service.createUser(user);
    }
}
