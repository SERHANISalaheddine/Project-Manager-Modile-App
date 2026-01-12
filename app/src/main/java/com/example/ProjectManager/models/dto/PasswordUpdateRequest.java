package com.example.ProjectManager.models.dto;

/**
 * Request DTO for updating user password.
 * PUT /api/v1/users/{id}/password
 */
public class PasswordUpdateRequest {
    private String currentPassword;
    private String newPassword;

    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
