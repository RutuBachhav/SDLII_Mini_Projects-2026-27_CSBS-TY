package com.labloop.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** All REST endpoints live under  http://localhost:8080/LabLoop/api/...  */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
