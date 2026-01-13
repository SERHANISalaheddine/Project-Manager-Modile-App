package com.example.ProjectManager.models.dto;

import java.util.Date;

public class TaskResponse {
    private long id;
    private String name;
    private String content;
    private String status;
    private long projectId;
    private String projectName;  // Set by mobile when available
    private String createdAt;
    private String updatedAt;
    private Assignee assignee;   // Matches backend RichTaskResponse
    private Long userId;         // Direct userId from backend TaskResponse (when assignee object is not provided)

    // Nested Assignee class to match backend structure
    public static class Assignee {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String profilePictureUrl;

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getProfilePictureUrl() { return profilePictureUrl; }

        public void setId(Long id) { this.id = id; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public void setEmail(String email) { this.email = email; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return name; }  // Alias for compatibility
    public String getContent() { return content; }
    public String getDescription() { return content; }  // Alias for compatibility
    public String getStatus() { return status; }
    public long getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public Assignee getAssignee() { return assignee; }

    // Convenience method to get userId from assignee or direct userId field
    public long getUserId() { 
        if (assignee != null && assignee.getId() != null) {
            return assignee.getId();
        }
        return userId != null ? userId : 0; 
    }
    
    // Convenience method to get assignee ID (nullable) - checks both assignee object and direct userId
    public Long getAssigneeId() { 
        if (assignee != null && assignee.getId() != null) {
            return assignee.getId();
        }
        return userId;
    }
    
    // Check if we have assignee details or just userId
    public boolean hasAssigneeDetails() {
        return assignee != null && assignee.getId() != null;
    }
    
    // Direct userId field setter/getter
    public Long getDirectUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setAssignee(Assignee assignee) { this.assignee = assignee; }
}
