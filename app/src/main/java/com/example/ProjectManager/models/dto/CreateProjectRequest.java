package com.example.ProjectManager.models.dto;

/**
 * Request DTO for creating a new project.
 * Used for POST /api/v1/projects endpoint.
 * Note: The owner is automatically set from the JWT token on the backend.
 */
public class CreateProjectRequest {
    private String name;
    private String description;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
