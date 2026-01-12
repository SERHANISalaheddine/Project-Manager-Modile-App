package com.example.ProjectManager.models.dto;

public class CreateTaskRequest {
    private String name;
    private String content;
    private String status; // TODO, IN_PROGRESS, DONE, ARCHIVED
    private long userId;
    private long projectId;

    // Constructeur
    public CreateTaskRequest(String name, String content, String status, long userId, long projectId) {
        this.name = name;
        this.content = content;
        this.status = status;
        this.userId = userId;
        this.projectId = projectId;
    }

    // Getters et setters
    public String getName() { return name; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public long getUserId() { return userId; }
    public long getProjectId() { return projectId; }

    public void setName(String name) { this.name = name; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
}
