package com.rest;

import com.entity.Category;
import com.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    public Response all() {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            List<Category> list = em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.name",
                    Category.class).getResultList();
            return Response.ok(list).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response byId(@PathParam("id") Integer id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Category c = em.find(Category.class, id);
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Category not found")).build();
            }
            return Response.ok(c).build();
        } finally {
            em.close();
        }
    }

    @POST
    public Response create(Category input) {
        if (input == null || input.getName() == null
                || input.getName().trim().length() < 2) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error",
                            "Category name must be at least 2 characters")).build();
        }
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Long dup = em.createQuery(
                    "SELECT COUNT(c) FROM Category c WHERE LOWER(c.name) = :n",
                    Long.class)
                    .setParameter("n", input.getName().trim().toLowerCase())
                    .getSingleResult();
            if (dup > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "Category already exists")).build();
            }
            Category c = new Category();
            c.setName(input.getName().trim());
            c.setDescription(input.getDescription());
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(c).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return Response.serverError()
                    .entity(Map.of("error", e.getMessage())).build();
        } finally {
            em.close();
        }
    }
}
