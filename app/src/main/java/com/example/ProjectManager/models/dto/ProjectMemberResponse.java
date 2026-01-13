package com.example.ProjectManager.models.dto;

/**
 * Mirrors backend RichProjectMembers DTO.
 * Response from GET /api/v1/projects/{id}/members
 */
public class ProjectMemberResponse {
    private Long projectId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;
    private String addedAt;

    public ProjectMemberResponse() {
    }

    // Getters
    public Long getProjectId() {
        return projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        // Alias for compatibility with adapters expecting getId()
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getAddedAt() {
        return addedAt;
    }

    // Setters
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
