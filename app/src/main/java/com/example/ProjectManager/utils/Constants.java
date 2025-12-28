package com.example.ProjectManager.utils;

/**
 * Centralized constants for API integration and configuration.
 */
public final class Constants {
    private Constants() {
    }

    // API Gateway base URL (dev)
    public static final String BASE_URL = "http://localhost:8080"; // TODO: swap via buildTypes when wiring backend

    // Route prefixes per gateway
    public static final String AUTH_PREFIX = "/api/v1/auth";
    public static final String USERS_PREFIX = "/api/v1/users";
    public static final String PROJECTS_PREFIX = "/api/v1/projects";
    public static final String TASKS_PREFIX = "/api/v1/tasks";
}
