package com.example.ProjectManager.utils;

/**
 * Centralized constants for API integration and configuration.
 */
public final class Constants {
    private Constants() {
    }

    // API Gateway base URL
    // Use 10.0.2.2 for emulator (maps to localhost on host machine)
    // Use the actual server IP/domain for real device
    public static final String BASE_URL = "http://10.0.2.2:8080";

    // Route prefixes per gateway
    public static final String AUTH_PREFIX = "/api/v1/auth";
    public static final String USERS_PREFIX = "/api/v1/users";
    public static final String PROJECTS_PREFIX = "/api/v1/projects";
    public static final String TASKS_PREFIX = "/api/v1/tasks";
}
