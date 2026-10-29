package com.rest;

import com.entity.Hostel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/hostels")
@Produces(MediaType.APPLICATION_JSON)
public class HostelResource {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("HostelPU");

    public static void closeFactory() {

        if (emf.isOpen()) {
            emf.close();
        }
    }

    @GET
    public Response getAllHostels() {

        EntityManager em = emf.createEntityManager();

        try {

            List<Hostel> hostels = em.createQuery(
                    "SELECT h FROM Hostel h",
                    Hostel.class
            ).getResultList();

            return Response.ok(hostels).build();

        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response getHostel(@PathParam("id") int id) {

        EntityManager em = emf.createEntityManager();

        try {

            Hostel hostel = em.find(Hostel.class, id);

            if (hostel == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Hostel not found")
                        .build();
            }

            return Response.ok(hostel).build();

        } finally {
            em.close();
        }
    }
}
