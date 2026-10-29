package com.rest;

import com.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("members")
public class MemberResource {

    private static final EntityManagerFactory emf = com.persistence.PersistenceManager.createEntityManagerFactory();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Member> getAllMembers() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Member.findAll", Member.class).getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMember(@PathParam("id") int id) {
        EntityManager em = emf.createEntityManager();
        try {
            Member member = em.find(Member.class, id);
            if (member == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(member).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMember(Member member) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(member);
            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(member).build();
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteMember(@PathParam("id") int id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Member member = em.find(Member.class, id);
            if (member != null) {
                em.remove(member);
            }
            em.getTransaction().commit();
            return Response.noContent().build();
        } finally {
            em.close();
        }
    }
}
