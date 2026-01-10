package com.example.ProjectManager.models.dto;

/**
 * Request DTO for adding a member to a project.
 */
public class AddMemberRequest {
    private Long userId;

    public AddMemberRequest() {
    }

    public AddMemberRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
