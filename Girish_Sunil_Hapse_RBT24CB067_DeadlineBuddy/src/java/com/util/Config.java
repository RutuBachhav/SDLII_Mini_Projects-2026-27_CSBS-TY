package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single source of configuration for the whole application.
 *
 * Each key is resolved as: JVM system property, then environment variable,
 * then {@code deadlinebuddy.properties} on the classpath, then a built-in
 * default. The environment variable name is the key upper-cased with dots
 * turned into underscores and prefixed with {@code DEADLINEBUDDY_}, so
 * {@code jdbc.password} is also readable as {@code DEADLINEBUDDY_JDBC_PASSWORD}.
 *
 * No credential appears anywhere else in the source.
 */
public final class Config {

    private static final Logger LOG = Logger.getLogger(Config.class.getName());
    private static final String FILE = "deadlinebuddy.properties";
    private static final String ENV_PREFIX = "DEADLINEBUDDY_";

    private static final Properties FROM_FILE = load();

    private Config() {
    }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(FILE)) {
            if (in == null) {
                LOG.log(Level.WARNING, "{0} not found on the classpath; using defaults", FILE);
            } else {
                p.load(in);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not read " + FILE + "; using defaults", e);
        }
        return p;
    }

    /** Resolves a key, falling back to {@code def} when nothing supplies it. */
    public static String get(String key, String def) {
        String v = System.getProperty(key);
        if (isBlank(v)) {
            v = System.getenv(ENV_PREFIX + key.toUpperCase().replace('.', '_'));
        }
        if (isBlank(v)) {
            v = FROM_FILE.getProperty(key);
        }
        return isBlank(v) ? def : v.trim();
    }

    public static int getInt(String key, int def) {
        String v = get(key, null);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "{0} is not a number: {1} - using {2}",
                    new Object[]{key, v, def});
            return def;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // -- the keys the application actually uses -----------------------------

    public static String jdbcDriver() {
        return get("jdbc.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static String jdbcUrl() {
        return get("jdbc.url",
                "jdbc:mysql://localhost:3306/deadlinebuddy_db?useSSL=false"
                + "&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
    }

    public static String jdbcUser() {
        return get("jdbc.user", "root");
    }

    public static String jdbcPassword() {
        return get("jdbc.password", "");
    }

    public static int poolMin() {
        return getInt("jdbc.pool.min", 2);
    }

    public static int poolMax() {
        return getInt("jdbc.pool.max", 20);
    }

    public static String ddlGeneration() {
        return get("jpa.ddl", "none");
    }

    public static String jpaLogLevel() {
        return get("jpa.log.level", "WARNING");
    }

    public static int pbkdf2Iterations() {
        return getInt("security.pbkdf2.iterations", 210000);
    }

    public static int sessionTimeoutMinutes() {
        return getInt("session.timeout.minutes", 60);
    }
}
