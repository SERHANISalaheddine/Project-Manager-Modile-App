package com.example.ProjectManager.models.dto;

/**
 * Request DTO for reset password endpoint.
 * POST /api/v1/auth/reset-password/{token}
 * Note: Token is passed in the URL path, not in the body.
 */
public class ResetPasswordRequest {
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
