package com.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class PersistenceManager {

    private PersistenceManager() {}

    public static EntityManagerFactory createEntityManagerFactory() {
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (user == null || user.isBlank()) {
            throw new IllegalStateException("DB_USER environment variable must be set.");
        }
        if (password == null) {
            throw new IllegalStateException("DB_PASSWORD environment variable must be set.");
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", password);

        String url = System.getenv("DB_URL");
        if (url != null && !url.isBlank()) {
            properties.put("jakarta.persistence.jdbc.url", url);
        }

        return Persistence.createEntityManagerFactory("LibraryPU", properties);
    }
}
