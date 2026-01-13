package com.example.ProjectManager.utils;

/**
 * Centralized constants for API integration and configuration.
 */
public final class Constants {
    private Constants() {
    }

    // API Gateway base URL
    // IMPORTANT: Choose the right URL based on your testing environment:
    //
    // For Android EMULATOR (maps to host machine's localhost):
    // public static final String BASE_URL = "http://10.0.2.2:8080";
    //
    // For PHYSICAL DEVICE on same WiFi network:
    // Replace with your computer's local IP (e.g., 192.168.0.100)
    // public static final String BASE_URL = "http://192.168.0.100:8080";
    //
    // Current setting - EMULATOR:
    public static final String BASE_URL = "http://10.0.2.2:8080";

    // Route prefixes per gateway
    public static final String AUTH_PREFIX = "/api/v1/auth";
    public static final String USERS_PREFIX = "/api/v1/users";
    public static final String PROJECTS_PREFIX = "/api/v1/projects";
    public static final String TASKS_PREFIX = "/api/v1/tasks";
}
