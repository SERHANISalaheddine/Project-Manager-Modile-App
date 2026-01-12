package com.example.ProjectManager.models;

import java.io.Serializable;

/**
 * Member model class representing a team member in the project management
 * system.
 * Contains member details such as name, role, and selection state.
 */
public class Member implements Serializable {

    private long id;
    private String name;
    private String role;
    private String avatarUrl;
    private boolean isSelected;

    // Default constructor
    public Member() {
    }

    // Constructor with basic fields
    public Member(long id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.isSelected = false;
    }

    // Full constructor
    public Member(long id, String name, String role, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.isSelected = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Returns the display text combining name and role
     * Format: "Name - Role"
     */
    public String getDisplayText() {
        return name + " - " + role;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Member member = (Member) o;
        return id == member.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
