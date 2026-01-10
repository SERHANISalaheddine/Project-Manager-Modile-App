package com.example.ProjectManager.api;

import com.example.ProjectManager.models.dto.AddMemberRequest;
import com.example.ProjectManager.models.dto.AuthResponseDto;
import com.example.ProjectManager.models.dto.CreateProjectRequest;
import com.example.ProjectManager.models.dto.LoginRequestDto;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.UserRequestDto;
import com.example.ProjectManager.models.dto.UserResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service interface with all endpoints.
 * All methods (except auth) require Authorization header with JWT token.
 */
public interface ApiService {

    // ============== AUTHENTICATION (Public - no token required) ==============

    @POST("/api/v1/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @POST("/api/v1/auth/register")
    Call<UserResponseDto> register(@Body UserRequestDto request);

    // ============== USERS (Require authentication) ==============

    @GET("/api/v1/users")
    Call<PageResponse<UserResponseDto>> getUsers(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/{id}")
    Call<UserResponseDto> getUser(@Path("id") long id);

    @DELETE("/api/v1/users/{id}")
    Call<Void> deleteUser(@Path("id") long id);

    // ============== PROJECTS (Require authentication) ==============

    @POST("/api/v1/projects")
    Call<ProjectResponse> createProject(@Body CreateProjectRequest request);

    @GET("/api/v1/projects/{id}")
    Call<ProjectResponse> getProject(@Path("id") long id);

    @PUT("/api/v1/projects/{id}")
    Call<ProjectResponse> updateProject(@Path("id") long id, @Body CreateProjectRequest request);

    @DELETE("/api/v1/projects/{id}")
    Call<Void> deleteProject(@Path("id") long id);

    // ============== PROJECT MEMBERS (Require authentication) ==============

    @POST("/api/v1/projects/{projectId}/members")
    Call<Void> addMemberToProject(@Path("projectId") long projectId, @Body AddMemberRequest request);

    @GET("/api/v1/projects/{projectId}/members")
    Call<PageResponse<UserResponseDto>> getProjectMembers(
            @Path("projectId") long projectId,
            @Query("page") int page,
            @Query("size") int size);

    @DELETE("/api/v1/projects/{projectId}/members/{userId}")
    Call<Void> removeMemberFromProject(@Path("projectId") long projectId, @Path("userId") long userId);
}
