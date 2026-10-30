package com.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Creates the JPA EntityManagerFactory once at startup and closes it at
 * shutdown. All servlets / resources share this instance instead of creating
 * one per servlet (a bug in the Employee sample).
 */
@WebListener
public class JpaUtil implements ServletContextListener {

    private static volatile EntityManagerFactory emf;

    static {
        try {
            // Force registration with DriverManager from the web-app
            // classloader (WEB-INF/lib). Required on GlassFish because
            // EclipseLink runs as a server module and DriverManager
            // otherwise throws "No suitable driver found".
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "MySQL JDBC driver not found in WEB-INF/lib: " + e.getMessage());
        }
    }

    public static EntityManagerFactory getEmf() {
        EntityManagerFactory local = emf;
        if (local == null) {
            synchronized (JpaUtil.class) {
                local = emf;
                if (local == null) {
                    local = Persistence.createEntityManagerFactory("ProductPU");
                    emf = local;
                }
            }
        }
        return local;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        getEmf();
        System.out.println("ProductPU EntityManagerFactory initialised");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("ProductPU EntityManagerFactory closed");
        }
    }
}
