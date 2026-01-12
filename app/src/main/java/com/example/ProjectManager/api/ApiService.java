package com.example.ProjectManager.api;

<<<<<<< Updated upstream
import com.example.ProjectManager.utils.Constants;
import java.util.Map;
// import retrofit2.Call;
// import retrofit2.http.Body;
// import retrofit2.http.DELETE;
// import retrofit2.http.GET;
// import retrofit2.http.PATCH;
// import retrofit2.http.POST;
// import retrofit2.http.PUT;
// import retrofit2.http.Path;
// import retrofit2.http.QueryMap;

/**
 * API endpoints interface (placeholders). Methods are declared but not used.
 * When backend integration starts, uncomment and implement as needed.
=======
import com.example.ProjectManager.models.dto.AddMemberRequest;
import com.example.ProjectManager.models.dto.AuthResponseDto;
import com.example.ProjectManager.models.dto.CreateProjectRequest;
import com.example.ProjectManager.models.dto.ForgotPasswordRequest;
import com.example.ProjectManager.models.dto.LoginRequestDto;
import com.example.ProjectManager.models.dto.MessageResponse;
import com.example.ProjectManager.models.dto.PageResponse;
import com.example.ProjectManager.models.dto.PasswordUpdateRequest;
import com.example.ProjectManager.models.dto.ProjectResponse;
import com.example.ProjectManager.models.dto.ResetPasswordRequest;
import com.example.ProjectManager.models.dto.UpdateUserRequest;
import com.example.ProjectManager.models.dto.UserRequestDto;
import com.example.ProjectManager.models.dto.UserResponseDto;

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
>>>>>>> Stashed changes
 */
public interface ApiService {

    // ==== AUTH ====
    // @POST(Constants.AUTH_PREFIX + "/login")
    // Call<AuthResponseDto> login(@Body LoginRequestDto request);
    //
    // @POST(Constants.AUTH_PREFIX + "/register")
    // Call<UserResponseDto> register(@Body UserRequestDto request);

    // ==== USERS ====
    // @GET(Constants.USERS_PREFIX)
    // Call<PageResponse<UserResponseDto>> getUsers(@QueryMap Map<String, String>
    // pageable);
    //
    // @GET(Constants.USERS_PREFIX + "/{id}")
    // Call<UserResponseDto> getUser(@Path("id") long id);
    //
    // @DELETE(Constants.USERS_PREFIX + "/{id}")
    // Call<Void> deleteUser(@Path("id") long id);

    // ==== PROJECTS ====
    // @POST(Constants.PROJECTS_PREFIX)
    // Call<ProjectResponse> createProject(@Body CreateProjectRequest request);
    //
    // @GET(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<ProjectResponse> getProject(@Path("id") long id);
    //
    // @PUT(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<ProjectResponse> updateProject(@Path("id") long id, @Body
    // UpdateProjectRequest request);
    //
    // @DELETE(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<Void> deleteProject(@Path("id") long id);
    //
    // @POST(Constants.PROJECTS_PREFIX + "/{id}/members")
    // Call<ProjectMemberResponse> addMember(@Path("id") long id, @Body
    // AddMemberRequest request);
    //
    // @DELETE(Constants.PROJECTS_PREFIX + "/{id}/members/{userId}")
    // Call<Void> removeMember(@Path("id") long id, @Path("userId") long userId);
    //
    // @GET(Constants.PROJECTS_PREFIX + "/{id}/members")
    // Call<PageResponse<RichProjectMembers>> listMembers(@Path("id") long id,
    // @QueryMap Map<String, String> pageable);

<<<<<<< Updated upstream
    // ==== TASKS ====
    // @POST(Constants.TASKS_PREFIX)
    // Call<TaskResponse> createTask(@Body CreateTaskRequest request);
    //
    // @GET(Constants.TASKS_PREFIX + "/{id}")
    // Call<TaskResponse> getTask(@Path("id") long id);
    //
    // @PUT(Constants.TASKS_PREFIX + "/{id}")
    // Call<TaskResponse> updateTask(@Path("id") long id, @Body UpdateTaskRequest
    // request);
    //
    // @DELETE(Constants.TASKS_PREFIX + "/{id}")
    // Call<Void> deleteTask(@Path("id") long id);
    //
    // @PATCH(Constants.TASKS_PREFIX + "/{id}/status")
    // Call<TaskResponse> updateTaskStatus(@Path("id") long id, @Body
    // UpdateTaskStatusRequest request);
    //
    // @GET(Constants.TASKS_PREFIX)
    // Call<PageResponse<RichTaskResponse>> filterTasks(@QueryMap Map<String,
    // String> query);

    // Note: Define PageResponse<T> later if needed when hooking backend.
=======
    @POST("/api/v1/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("/api/v1/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

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
    Call<PageResponse<UserResponseDto>> getProjectMembers(
            @Path("projectId") long projectId,
            @Query("page") int page,
            @Query("size") int size);

    @DELETE("/api/v1/projects/{projectId}/members/{userId}")
    Call<Void> removeMemberFromProject(@Path("projectId") long projectId, @Path("userId") long userId);
>>>>>>> Stashed changes
}
