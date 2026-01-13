package com.example.ProjectManager.api;

import com.example.ProjectManager.models.dto.AddMemberRequest;
import com.example.ProjectManager.models.dto.AuthResponseDto;
import com.example.ProjectManager.models.dto.CreateProjectRequest;
import com.example.ProjectManager.models.dto.CreateTaskRequest;
import com.example.ProjectManager.models.dto.ForgotPasswordRequest;
import com.example.ProjectManager.models.dto.LoginRequestDto;
import com.example.ProjectManager.models.dto.MessageResponse;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.PasswordUpdateRequest;
import com.example.ProjectManager.models.dto.ProjectMemberResponse;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.ResetPasswordRequest;
import com.example.ProjectManager.models.dto.TaskResponse;
import com.example.ProjectManager.models.dto.UpdateUserRequest;
import com.example.ProjectManager.models.dto.UserRequestDto;
import com.example.ProjectManager.models.dto.UserResponseDto;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;

import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service interface with all endpoints.
 * Base URL: http://localhost:8080 (use 10.0.2.2:8080 for emulator)
 * All methods (except auth) require Authorization header with JWT token.
 */
public interface ApiService {

    // ============== AUTHENTICATION (Public - no token required) ==============

    @POST("/api/v1/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @POST("/api/v1/auth/register")
    Call<UserResponseDto> register(@Body UserRequestDto request);

    @POST("/api/v1/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("/api/v1/auth/reset-password/{token}")
    Call<MessageResponse> resetPassword(@Path("token") String token, @Body ResetPasswordRequest request);

    @GET("/api/v1/auth/verify-email")
    Call<MessageResponse> verifyEmail(@Query("token") String token);

    // ============== USERS (Require authentication) ==============

    @GET("/api/v1/users")
    Call<PageResponse<UserResponseDto>> getUsers(@Query("page") int page, @Query("size") int size);

    @GET("/api/v1/users/{id}")
    Call<UserResponseDto> getUser(@Path("id") long id);

    @PATCH("/api/v1/users/{id}")
    Call<UserResponseDto> updateUser(@Path("id") long id, @Body UpdateUserRequest request);

    @DELETE("/api/v1/users/{id}")
    Call<Void> deleteUser(@Path("id") long id);

    @PUT("/api/v1/users/{id}/password")
    Call<MessageResponse> updatePassword(@Path("id") long id, @Body PasswordUpdateRequest request);

    @Multipart
    @POST("/api/v1/users/me/profile-picture")
    Call<UserResponseDto> uploadProfilePicture(@Part MultipartBody.Part file);

    @DELETE("/api/v1/users/me/profile-picture")
    Call<MessageResponse> deleteProfilePicture();

    // ============== PROJECTS (Require authentication) ==============

    @POST("/api/v1/projects")
    Call<ProjectResponse> createProject(@Body CreateProjectRequest request);

    @GET("/api/v1/projects/{id}")
    Call<ProjectResponse> getProject(@Path("id") long id);

    @PUT("/api/v1/projects/{id}")
    Call<ProjectResponse> updateProject(@Path("id") long id, @Body CreateProjectRequest request);

    @DELETE("/api/v1/projects/{id}")
    Call<Void> deleteProject(@Path("id") long id);

    @GET("/api/v1/projects/owner/{userId}")
    Call<PageResponse<ProjectResponse>> getProjectsByOwner(
            @Path("userId") long userId,
            @Query("page") int page,
            @Query("size") int size);

    @GET("/api/v1/projects/member/{userId}")
    Call<PageResponse<ProjectResponse>> getProjectsByMember(
            @Path("userId") long userId,
            @Query("page") int page,
            @Query("size") int size);

    // ============== PROJECT MEMBERS (Require authentication) ==============

    @POST("/api/v1/projects/{projectId}/members")
    Call<Void> addMemberToProject(@Path("projectId") long projectId, @Body AddMemberRequest request);

    @GET("/api/v1/projects/{projectId}/members")
    Call<PageResponse<ProjectMemberResponse>> getProjectMembers(
            @Path("projectId") long projectId,
            @Query("page") int page,
            @Query("size") int size);

    @DELETE("/api/v1/projects/{projectId}/members/{userId}")
    Call<Void> removeMemberFromProject(@Path("projectId") long projectId, @Path("userId") long userId);

    // ============== TASKS ==============

    // Créer une tâche
    @POST("/api/v1/tasks")
    Call<TaskResponse> createTask(@Body CreateTaskRequest request);

    // Récupérer une tâche par ID
    @GET("/api/v1/tasks/{taskId}")
    Call<TaskResponse> getTask(@Path("taskId") long taskId);

    // Récupérer toutes les tâches
    @GET("/api/v1/tasks")
    Call<PageResponse<TaskResponse>> getAllTasks(
            @Query("page") int page,
            @Query("size") int size,
            @Query("userId") Long userId,
            @Query("projectId") Long projectId,
            @Query("status") String status
    );

    // Mettre à jour une tâche
    @PUT("/api/v1/tasks/{taskId}")
    Call<TaskResponse> updateTask(@Path("taskId") long taskId, @Body CreateTaskRequest request);

    // Mettre à jour uniquement le status
    @PATCH("/api/v1/tasks/{taskId}/status")
    Call<TaskResponse> updateTaskStatus(@Path("taskId") long taskId, @Body Map<String, String> status);

    // Supprimer une tâche
    @DELETE("/api/v1/tasks/{taskId}")
    Call<Void> deleteTask(@Path("taskId") long taskId);

}