package com.security;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Detects a fresh installation and publishes a one-time setup key in the
 * server log. The key is removed as soon as the first Admin is created.
 */
@WebListener
public class BootstrapListener implements ServletContextListener {

    public static final String SETUP_KEY_ATTR = "rbacSetupKey";
    public static final String SETUP_KEY_PROPERTY = "hostel.bootstrap.key";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try {
            long users = new UserDAO().countUsers();
            if (users == 0) {
                String configured = System.getProperty(SETUP_KEY_PROPERTY);
                if (configured != null && !configured.trim().isEmpty()) {
                    context.setAttribute(SETUP_KEY_ATTR, configured.trim());
                    context.log("RBAC initialization pending; bootstrap key "
                            + "provided through the " + SETUP_KEY_PROPERTY
                            + " system property.");
                } else {
                    String key = generateKey();
                    context.setAttribute(SETUP_KEY_ATTR, key);
                    context.log("RBAC_SETUP_KEY=" + key
                            + " (one-time bootstrap key; open /setup)");
                }
            } else {
                context.log("RBAC initialization already completed for "
                        + users + " user account(s). Setup endpoint disabled.");
            }
        } catch (Exception ex) {
            context.log("RBAC startup check failed; the application will "
                    + "continue and setup will retry when opened.", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        JpaUtil.close();
    }

    public static String generateKey() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }
}
