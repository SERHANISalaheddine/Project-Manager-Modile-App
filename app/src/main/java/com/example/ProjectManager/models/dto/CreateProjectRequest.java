package com.example.ProjectManager.models.dto;

/**
 * Request DTO for creating a new project.
 */
public class CreateProjectRequest {
    private String name;
    private String description;
    private Long ownerId;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String name, String description, Long ownerId) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
