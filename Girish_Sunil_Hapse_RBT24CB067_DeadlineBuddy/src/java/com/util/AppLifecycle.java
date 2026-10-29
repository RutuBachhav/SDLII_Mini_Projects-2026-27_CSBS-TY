package com.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts the persistence unit when the web application deploys and closes it
 * on undeploy, so redeploying does not leak a connection pool.
 *
 * A failure here is logged but not rethrown: the application still starts and
 * every page then reports a clear database error, which is far easier to
 * diagnose than a deployment that silently refuses to come up.
 */
@WebListener
public class AppLifecycle implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppLifecycle.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("appName", "DeadlineBuddy");
        try {
            Jpa.start();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "could not start the persistence unit - check "
                    + "MySQL and the jdbc.* settings in deadlinebuddy.properties", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Jpa.stop();
        LOG.info("DeadlineBuddy stopped");
    }
}
