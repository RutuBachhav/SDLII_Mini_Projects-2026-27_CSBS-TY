package com.security;

import com.entity.AppRole;
import com.entity.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;

/**
 * All reads and writes for application users and their roles.
 */
public class UserDAO {

    private static final String CACHE_HINT = "eclipselink.cache-usage";
    private static final String NO_CACHE = "DoNotCheckCache";

    public long countUsers() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT COUNT(u) FROM AppUser u", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public AppUser findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        EntityManager em = JpaUtil.em();
        try {
            TypedQuery<AppUser> query = em.createQuery(
                    "SELECT DISTINCT u FROM AppUser u "
                    + "LEFT JOIN FETCH u.roles "
                    + "WHERE LOWER(u.username) = :username",
                    AppUser.class);
            query.setParameter("username",
                    username.trim().toLowerCase(Locale.ROOT));
            query.setHint(CACHE_HINT, NO_CACHE);
            List<AppUser> users = query.getResultList();
            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    public List<AppUser> findAll() {
        EntityManager em = JpaUtil.em();
        try {
            TypedQuery<AppUser> query = em.createQuery(
                    "SELECT DISTINCT u FROM AppUser u "
                    + "LEFT JOIN FETCH u.roles ORDER BY u.userId",
                    AppUser.class);
            query.setHint(CACHE_HINT, NO_CACHE);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public AppUser createUser(String username, String rawPassword,
                              String roleName) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanRole = roleName == null
                ? "" : roleName.trim().toUpperCase(Locale.ROOT);

        if (!AuthorizationService.validRole(cleanRole)) {
            throw new IllegalArgumentException("Unknown role.");
        }

        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            Long existing = em.createQuery(
                    "SELECT COUNT(u) FROM AppUser u "
                    + "WHERE LOWER(u.username) = :username", Long.class)
                    .setParameter("username",
                            cleanUsername.toLowerCase(Locale.ROOT))
                    .getSingleResult();
            if (existing > 0) {
                throw new IllegalStateException(
                        "That username is already in use.");
            }

            List<AppRole> matches = em.createQuery(
                    "SELECT r FROM AppRole r WHERE r.roleName = :name",
                    AppRole.class)
                    .setParameter("name", cleanRole)
                    .getResultList();
            AppRole role;
            if (matches.isEmpty()) {
                role = new AppRole(cleanRole);
                em.persist(role);
            } else {
                role = matches.get(0);
            }

            AppUser user = new AppUser();
            user.setUsername(cleanUsername);
            user.setPasswordHash(PasswordUtil.hash(rawPassword));
            user.setActive(true);
            user.getRoles().add(role);

            em.persist(user);
            em.getTransaction().commit();

            em.getEntityManagerFactory().getCache().evict(AppUser.class);
            return user;

        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public boolean setActive(long userId, boolean active) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            AppUser user = em.find(AppUser.class, userId);
            if (user == null) {
                em.getTransaction().rollback();
                return false;
            }
            user.setActive(active);
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evict(AppUser.class);
            return true;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void ensureRoles() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            String[] names = {
                AuthorizationService.ROLE_ADMIN,
                AuthorizationService.ROLE_WARDEN,
                AuthorizationService.ROLE_VIEWER
            };
            for (String name : names) {
                Long count = em.createQuery(
                        "SELECT COUNT(r) FROM AppRole r "
                        + "WHERE r.roleName = :name", Long.class)
                        .setParameter("name", name)
                        .getSingleResult();
                if (count == 0) {
                    em.persist(new AppRole(name));
                }
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
