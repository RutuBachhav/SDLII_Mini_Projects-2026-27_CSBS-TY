package com.web;

import com.util.SchemaInitializer;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            SchemaInitializer.init();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not prepare the movie_booking database", e);
            event.getServletContext().setAttribute("dbError",
                    "MySQL connection failed. Check USER and PASSWORD in com.util.DBConnection. " + e.getMessage());
        }
    }
}
