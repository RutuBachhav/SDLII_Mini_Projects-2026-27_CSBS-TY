package com.rest;

import com.entity.Category;
import com.entity.Product;
import com.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Full CRUD (the Employee sample only had 2 read-only GETs and leaked the
 * EntityManagerFactory as a static field with no shutdown hook).
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

     @GET
    public Response all(@QueryParam("q") String q,
                        @QueryParam("categoryId") Integer categoryId) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE 1=1");
            if (q != null && !q.isBlank()) {
                jpql.append(" AND (LOWER(p.name) LIKE :q OR LOWER(p.sku) LIKE :q)");
            }
            if (categoryId != null) {
                jpql.append(" AND p.category.id = :cid");
            }
            jpql.append(" ORDER BY p.id DESC");
            TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
            if (q != null && !q.isBlank()) {
                query.setParameter("q", "%" + q.toLowerCase() + "%");
            }
            if (categoryId != null) {
                query.setParameter("cid", categoryId);
            }
            List<Product> list = query.getResultList();
            return Response.ok(list).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response byId(@PathParam("id") Long id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Product p = em.find(Product.class, id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Product not found")).build();
            }
            return Response.ok(p).build();
        } finally {
            em.close();
        }
    }

    @POST
    public Response create(Product input) {
        String err = validate(input);
        if (err != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", err)).build();
        }
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Long dup = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE LOWER(p.sku) = :s",
                    Long.class)
                    .setParameter("s", input.getSku().toLowerCase())
                    .getSingleResult();
            if (dup > 0) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "SKU already exists")).build();
            }
            Category cat = null;
            if (input.getCategory() != null && input.getCategory().getId() != null) {
                cat = em.find(Category.class, input.getCategory().getId());
                if (cat == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "Category does not exist")).build();
                }
            }
            Product p = new Product();
            p.setSku(input.getSku());
            p.setName(input.getName());
            p.setPrice(input.getPrice());
            p.setQuantity(input.getQuantity());
            p.setDescription(input.getDescription());
            p.setCategory(cat);
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(p).build();
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

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Product input) {
        String err = validate(input);
        if (err != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", err)).build();
        }
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Product p = em.find(Product.class, id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Product not found")).build();
            }
            em.getTransaction().begin();
            p.setSku(input.getSku());
            p.setName(input.getName());
            p.setPrice(input.getPrice());
            p.setQuantity(input.getQuantity());
            p.setDescription(input.getDescription());
            if (input.getCategory() != null && input.getCategory().getId() != null) {
                Category cat = em.find(Category.class,
                        input.getCategory().getId());
                if (cat == null) {
                    em.getTransaction().rollback();
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "Category does not exist")).build();
                }
                p.setCategory(cat);
            }
            em.getTransaction().commit();
            return Response.ok(p).build();
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

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            Product p = em.find(Product.class, id);
            if (p == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Product not found")).build();
            }
            em.getTransaction().begin();
            em.remove(p);
            em.getTransaction().commit();
            return Response.noContent().build();
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

    private static String validate(Product p) {
        if (p == null) {
            return "Request body is required.";
        }
        if (p.getSku() == null || !p.getSku().matches("^[A-Za-z0-9-]{3,20}$")) {
            return "SKU must be 3-20 chars (letters, digits, dash).";
        }
        if (p.getName() == null || p.getName().length() < 3
                || p.getName().length() > 80) {
            return "Product name must be 3-80 characters.";
        }
        if (p.getPrice() == null || p.getPrice() < 0.01) {
            return "Price must be greater than 0.";
        }
        if (p.getQuantity() == null || p.getQuantity() < 0) {
            return "Quantity must be 0 or more.";
        }
        return null;
    }
}
