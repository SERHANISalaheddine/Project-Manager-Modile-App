package com.example.ProjectManager.models.dto;

/**
 * Request DTO for updating user info.
 * PATCH /api/v1/users/{id}
 * All fields are optional - only include fields you want to update.
 */
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String email;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
