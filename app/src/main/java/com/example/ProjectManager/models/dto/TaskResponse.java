package com.example.ProjectManager.models.dto;

public class TaskResponse {
    private long id;
    private String name;
    private String content;
    private String status;
    private long userId;
    private long projectId;

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public long getUserId() { return userId; }
    public long getProjectId() { return projectId; }

    // Setters si besoin
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
}
