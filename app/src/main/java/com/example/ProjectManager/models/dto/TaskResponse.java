package com.example.ProjectManager.models.dto;

import java.util.Date;

public class TaskResponse {
    private long id;
    private String name;
    private String title;       // Added to match web app
    private String content;
    private String description;  // Added
    private String status;
    private String priority;     // Added for priority support
    private long userId;
    private Long assigneeId;     // Added (nullable)
    private long projectId;
    private String dueDate;      // Added
    private ProjectResponse project; // Added for nested project info

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title != null ? title : name; }
    public String getContent() { return content; }
    public String getDescription() { return description != null ? description : content; }
    public String getStatus() { return status; }
    public String getPriority() { return priority != null ? priority : "MEDIUM"; }
    public long getUserId() { return userId; }
    public Long getAssigneeId() { return assigneeId; }
    public long getProjectId() { return projectId; }
    public String getDueDate() { return dueDate; }
    public ProjectResponse getProject() { return project; }
    
    // Parse due date as Date object
    public Date getDueDateAsDate() {
        if (dueDate == null || dueDate.isEmpty()) return null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            return sdf.parse(dueDate);
        } catch (Exception e) {
            return null;
        }
    }

    // Setters si besoin
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setProject(ProjectResponse project) { this.project = project; }
}
