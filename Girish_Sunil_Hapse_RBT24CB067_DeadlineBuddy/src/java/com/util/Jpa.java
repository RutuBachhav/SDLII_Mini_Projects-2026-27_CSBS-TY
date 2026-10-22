package com.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns the single {@link EntityManagerFactory} and defines the transaction
 * boundaries for the application.
 *
 * The persistence unit is RESOURCE_LOCAL, so transactions are explicit. Every
 * database access goes through {@link #read} or {@link #write}: the entity
 * manager is always closed, a write is always either committed or rolled back,
 * and no DAO or servlet has to remember to do it.
 *
 * The JDBC URL, user and password in {@code persistence.xml} are only defaults;
 * they are overridden here from {@link Config} at startup, which is what keeps
 * credentials out of the source and lets a lab machine override them with an
 * environment variable.
 */
public final class Jpa {

    private static final Logger LOG = Logger.getLogger(Jpa.class.getName());
    public static final String UNIT_NAME = "DeadlineBuddyPU";

    private static EntityManagerFactory emf;

    private Jpa() {
    }

    /** Builds the factory. Called once, from {@link AppLifecycle}. */
    public static synchronized void start() {
        if (emf != null) {
            return;
        }
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.driver", Config.jdbcDriver());
        overrides.put("jakarta.persistence.jdbc.url", Config.jdbcUrl());
        overrides.put("jakarta.persistence.jdbc.user", Config.jdbcUser());
        overrides.put("jakarta.persistence.jdbc.password", Config.jdbcPassword());
        overrides.put("eclipselink.ddl-generation", Config.ddlGeneration());
        overrides.put("eclipselink.logging.level", Config.jpaLogLevel());
        overrides.put("eclipselink.jdbc.connections.min", String.valueOf(Config.poolMin()));
        overrides.put("eclipselink.jdbc.connections.max", String.valueOf(Config.poolMax()));

        emf = Persistence.createEntityManagerFactory(UNIT_NAME, overrides);
        LOG.log(Level.INFO, "persistence unit {0} started against {1}",
                new Object[]{UNIT_NAME, Config.jdbcUrl()});
    }

    /** Closes the factory and its connection pool. */
    public static synchronized void stop() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    private static synchronized EntityManagerFactory factory() {
        if (emf == null) {
            // Only happens if a servlet is hit before the listener ran, or
            // after a failed startup; building it lazily keeps the app usable.
            start();
        }
        return emf;
    }

    /** Runs a read-only unit of work. */
    public static <T> T read(Function<EntityManager, T> work) {
        EntityManager em = factory().createEntityManager();
        try {
            return work.apply(em);
        } catch (RuntimeException e) {
            throw translate(e);
        } finally {
            em.close();
        }
    }

    /** Runs a unit of work inside a transaction, rolling back on any failure. */
    public static <T> T write(Function<EntityManager, T> work) {
        EntityManager em = factory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (RuntimeException rollbackFailure) {
                    LOG.log(Level.WARNING, "rollback failed", rollbackFailure);
                }
            }
            throw translate(e);
        } finally {
            em.close();
        }
    }

    /**
     * Keeps application-level exceptions (validation, conflict) as they are and
     * wraps anything else - driver errors, connection failures, constraint
     * violations - in {@link DataAccessException}, so the servlet layer can
     * show a sane message instead of a stack trace.
     */
    private static RuntimeException translate(RuntimeException e) {
        if (e instanceof AppException || e instanceof DataAccessException) {
            return e;
        }
        LOG.log(Level.SEVERE, "database operation failed", e);
        return new DataAccessException("The database could not complete the request.", e);
    }
}
