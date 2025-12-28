package com.example.ProjectManager.models.dto;

/**
 * Mirrors backend AuthResponseDto.
 */
public class AuthResponseDto {
    private String token;
    private String type; // typically "Bearer"

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String type) {
        this.token = token;
        this.type = type;
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
}
