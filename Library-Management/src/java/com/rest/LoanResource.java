package com.rest;

import com.entity.Loan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("loans")
public class LoanResource {

    private static final EntityManagerFactory emf = com.persistence.PersistenceManager.createEntityManagerFactory();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Loan> getAllLoans() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Loan.findAll", Loan.class).getResultList();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoan(@PathParam("id") int id) {
        EntityManager em = emf.createEntityManager();
        try {
            Loan loan = em.find(Loan.class, id);
            if (loan == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(loan).build();
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteLoan(@PathParam("id") int id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Loan loan = em.find(Loan.class, id);
            if (loan != null) {
                em.remove(loan);
            }
            em.getTransaction().commit();
            return Response.noContent().build();
        } finally {
            em.close();
        }
    }
}
