package com.example.ProjectManager.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Project model class representing a project in the task management system.
 * Contains project details such as title, description, members, and dates.
 */
public class Project implements Serializable {

    private int id;
    private String title;
    private String description;
    private List<Member> members;
    private Date createdAt;
    private Date dueDate;
    private String status; // "created", "in_progress", "completed"
    private int creatorId;

    // Default constructor
    public Project() {
        this.members = new ArrayList<>();
        this.createdAt = new Date();
        this.status = "created";
    }

    // Constructor with basic fields
    public Project(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    // Constructor for API response (Long id converted to int)
    public Project(Long id, String name, String description) {
        this();
        this.id = id != null ? id.intValue() : 0;
        this.title = name;
        this.description = description;
    }

    // Constructor with members
    public Project(String title, String description, List<Member> members) {
        this(title, description);
        this.members = members != null ? members : new ArrayList<>();
    }

    // Full constructor
    public Project(int id, String title, String description, List<Member> members,
            Date createdAt, Date dueDate, String status, int creatorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.status = status;
        this.creatorId = creatorId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Add a member to the project
     */
    public void addMember(Member member) {
        if (member != null && !members.contains(member)) {
            members.add(member);
        }
    }

    /**
     * Remove a member from the project
     */
    public void removeMember(Member member) {
        members.remove(member);
    }

    /**
     * Get the count of members in this project
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Check if the project has any members
     */
    public boolean hasMembers() {
        return !members.isEmpty();
    }

    /**
     * Validate if the project has required fields
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", membersCount=" + getMemberCount() +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
