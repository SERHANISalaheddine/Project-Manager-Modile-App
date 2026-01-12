package com.example.ProjectManager.models.dto;

/**
 * Generic message response from API.
 * Used for endpoints that return simple message responses.
 */
public class MessageResponse {
    private String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
