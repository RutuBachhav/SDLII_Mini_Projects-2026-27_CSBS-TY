package com.rest;

import com.entity.Room;
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

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("HostelPU");

    public static void closeFactory() {

        if (emf.isOpen()) {
            emf.close();
        }
    }

    @GET
    public Response getAllRooms() {

        EntityManager em = emf.createEntityManager();

        try {

            List<Room> rooms = em.createQuery(
                    "SELECT r FROM Room r",
                    Room.class
            ).getResultList();

            return Response.ok(rooms).build();

        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") Long id) {

        EntityManager em = emf.createEntityManager();

        try {

            Room room = em.find(Room.class, id);

            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Room not found")
                        .build();
            }

            return Response.ok(room).build();

        } finally {
            em.close();
        }
    }
}
