package com.dao;

import com.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;

/**
 * Queries against the users table.
 *
 * Every method takes the {@link EntityManager} it should use. The transaction
 * boundary lives one layer up, in {@code com.util.Jpa} as called by the
 * services, so a DAO never commits and two DAO calls can share one
 * transaction.
 */
public class UserDao {

    /** Looks a user up by email, case-insensitively. */
    public Optional<User> findByEmail(EntityManager em, String email) {
        if (email == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(em.createNamedQuery("User.byEmail", User.class)
                    .setParameter("email", email.trim().toLowerCase())
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(EntityManager em, Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(em.find(User.class, id));
    }

    /**
     * Pre-check for the registration form, so the user gets "that email is
     * already registered" next to the field instead of a constraint violation.
     * The unique index on the column is still what guarantees it.
     */
    public boolean emailExists(EntityManager em, String email) {
        if (email == null) {
            return false;
        }
        Long count = em.createNamedQuery("User.countByEmail", Long.class)
                .setParameter("email", email.trim().toLowerCase())
                .getSingleResult();
        return count != null && count > 0;
    }

    public User insert(EntityManager em, User user) {
        em.persist(user);
        em.flush();
        return user;
    }
}
