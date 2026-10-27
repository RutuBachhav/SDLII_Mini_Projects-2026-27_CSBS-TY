package com.campuscare.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.TimeZone;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
}
