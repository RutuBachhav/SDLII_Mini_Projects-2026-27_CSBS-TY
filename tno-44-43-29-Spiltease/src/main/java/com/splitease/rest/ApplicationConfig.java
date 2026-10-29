package com.splitease.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activates JAX-RS (Jersey inside GlassFish). Every resource class in this
 * package is published under http://localhost:8080/SplitEase/api/...
 */
@ApplicationPath("api")
public class ApplicationConfig extends Application {
}
