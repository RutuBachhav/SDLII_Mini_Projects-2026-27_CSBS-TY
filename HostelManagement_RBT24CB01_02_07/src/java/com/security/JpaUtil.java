package com.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * One shared persistence unit for authentication and user management.
 * Existing CRUD servlets keep their own factories to preserve their behavior.
 */
public final class JpaUtil {

    private static final String UNIT = "HostelPU";

    private static volatile EntityManagerFactory factory;

    private JpaUtil() {
    }

    public static EntityManagerFactory factory() {
        EntityManagerFactory value = factory;
        if (value == null) {
            synchronized (JpaUtil.class) {
                value = factory;
                if (value == null) {
                    value = Persistence.createEntityManagerFactory(UNIT);
                    factory = value;
                }
            }
        }
        return value;
    }

    public static EntityManager em() {
        return factory().createEntityManager();
    }

    public static void close() {
        EntityManagerFactory value = factory;
        if (value != null) {
            factory = null;
            if (value.isOpen()) {
                value.close();
            }
        }
    }
}
