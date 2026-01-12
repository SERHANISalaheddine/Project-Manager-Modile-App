package com.example.ProjectManager.models.dto;

/**
 * Mirrors backend AuthResponseDto.
 * Response from /api/v1/auth/login endpoint.
 */
public class AuthResponseDto {
    private String token;
    private String type; // typically "Bearer"
    private Long userId; // User ID returned from login

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String type, Long userId) {
        this.token = token;
        this.type = type;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
