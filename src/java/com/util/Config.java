package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Single place where configuration is read. Nothing else in the application
 * knows a JDBC URL, a password or a filesystem path.
 *
 * Resolution order for every key, first hit wins:
 *   1. JVM system property           -foundit.db.url=...
 *   2. environment variable          FOUNDIT_DB_URL
 *   3. foundit.properties on the classpath (WEB-INF/classes)
 *   4. the default passed by the caller
 *
 * That order is what lets the same WAR run on another machine, or in a lab
 * where the MySQL password is different, without editing any Java file.
 */
public final class Config {

    private static final Properties FILE = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader()
                .getResourceAsStream("foundit.properties")) {
            if (in != null) {
                FILE.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read foundit.properties", e);
        }
    }

    private Config() {
    }

    public static String get(String key, String fallback) {
        String fromJvm = System.getProperty(key);
        if (isSet(fromJvm)) {
            return fromJvm.trim();
        }
        String fromEnv = System.getenv(key.toUpperCase().replace('.', '_'));
        if (isSet(fromEnv)) {
            return fromEnv.trim();
        }
        String fromFile = FILE.getProperty(key);
        if (isSet(fromFile)) {
            return fromFile.trim();
        }
        return fallback;
    }

    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
