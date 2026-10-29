package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class UserDAO {

    public Users validateLogin(String username, String password) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            TypedQuery<Users> query = em.createQuery(
                    "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password",
                    Users.class);
            query.setParameter("username", username);
            query.setParameter("password", password);

            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public boolean usernameExists(String username) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.username = :username",
                    Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
